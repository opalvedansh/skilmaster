import { useCallback, useEffect, useState } from "react";
import AsyncBlock from "../AsyncBlock";
import { useToast } from "../Toast";
import { Alert, Button, Card, PageHeader } from "../ui/Basics";
import { DataTable, Pagination } from "../ui/DataTable";
import { Input, Select, Textarea } from "../ui/Form";
import { ConfirmDialog, Modal } from "../ui/Modal";
import useDebounce from "../../hooks/useDebounce";
import { errorMessage, fieldErrors } from "../../services/api";

/**
 * Generic admin CRUD screen.
 *  fields:  [{ name, label, type: text|textarea|select|password, options?, required?: true|"create", showOn?: "create"|"edit", validate?(value, form) }]
 *  filters: [{ name, label, options: [{ value, label }], placeholder }]
 *  fetchPage({ page, size, search, filters }) -> { items, totalPages, totalElements }
 */
export default function ResourceManager({ title, subtitle, entity, columns, fields, filters = [], fetchPage, onCreate, onUpdate, onDelete,
                                          toForm, toPayload, deleteMessage, canDelete = () => true, deleteHint, pageSize = 10, searchPlaceholder = "Search" }) {
  const toast = useToast();
  const [search, setSearch] = useState("");
  const [filterValues, setFilterValues] = useState({});
  const [page, setPage] = useState(0);
  const [state, setState] = useState({ data: null, loading: true, error: null });
  const [tick, setTick] = useState(0);
  const [form, setForm] = useState(null); // { mode, id?, values }
  const [deleting, setDeleting] = useState(null);
  const [busy, setBusy] = useState(false);
  const debounced = useDebounce(search, 300);

  useEffect(() => { setPage(0); }, [debounced, filterValues]);

  useEffect(() => {
    let active = true;
    setState((s) => ({ ...s, loading: true, error: null }));
    fetchPage({ page, size: pageSize, search: debounced, filters: filterValues })
      .then((data) => active && setState({ data, loading: false, error: null }))
      .catch((err) => active && setState({ data: null, loading: false, error: errorMessage(err) }));
    return () => { active = false; };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, debounced, filterValues, tick]);

  const reload = useCallback(() => setTick((t) => t + 1), []);

  const confirmDelete = async () => {
    if (busy) return;
    setBusy(true);
    try {
      await onDelete(deleting);
      toast.success(`${entity} deleted`);
      if (state.data?.items.length === 1 && page > 0) setPage(page - 1);
      setDeleting(null); reload();
    } catch (err) { toast.error(errorMessage(err)); setDeleting(null); }
    finally { setBusy(false); }
  };

  return (
    <>
      <PageHeader title={title} subtitle={subtitle} actions={onCreate && <Button onClick={() => setForm({ mode: "create", values: toForm(null) })}>Add {entity.toLowerCase()}</Button>} />
      <Card className="filters">
        <Input label="Search" placeholder={searchPlaceholder} value={search} onChange={(e) => setSearch(e.target.value)} />
        {filters.map((f) => (
          <Select key={f.name} label={f.label} value={filterValues[f.name] || ""} placeholder={f.placeholder || "All"} options={f.options}
                  onChange={(e) => setFilterValues((v) => ({ ...v, [f.name]: e.target.value }))} />
        ))}
      </Card>
      <div className="mt">
        <AsyncBlock loading={state.loading} error={state.error} data={state.data} onRetry={reload} isEmpty={state.data?.items.length === 0}
                    emptyTitle={`No ${entity.toLowerCase()} records found`} emptyText="Adjust the search or filters, or add a new record.">
          <Card>
            <DataTable columns={columns} rows={state.data?.items || []} actions={(row) => (
              <>
                <Button size="sm" variant="secondary" onClick={() => setForm({ mode: "edit", id: row.id, values: toForm(row) })}>Edit</Button>
                <Button size="sm" variant="danger" disabled={!canDelete(row)} title={!canDelete(row) ? deleteHint : undefined} onClick={() => setDeleting(row)}>Delete</Button>
              </>
            )} />
            <div className="row-between mt-sm">
              <span className="muted small">{state.data?.totalElements} total</span>
              <Pagination page={page} totalPages={state.data?.totalPages || 0} onChange={setPage} />
            </div>
          </Card>
        </AsyncBlock>
      </div>

      {form && <RecordForm entity={entity} fields={fields} state={form} toPayload={toPayload} toast={toast}
        onClose={() => setForm(null)} onSaved={() => { setForm(null); reload(); }}
        save={(payload) => (form.mode === "create" ? onCreate(payload) : onUpdate(form.id, payload))} />}
      {deleting && <ConfirmDialog message={deleteMessage(deleting)} loading={busy} onConfirm={confirmDelete} onCancel={() => setDeleting(null)} />}
    </>
  );
}

function RecordForm({ entity, fields, state, toPayload, save, onClose, onSaved, toast }) {
  const [values, setValues] = useState(state.values);
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState("");
  const [saving, setSaving] = useState(false);
  const mode = state.mode;
  const visible = fields.filter((f) => !f.showOn || f.showOn === mode);

  const submit = async (e) => {
    e.preventDefault();
    if (saving) return;
    const v = {};
    for (const f of visible) {
      const val = values[f.name];
      const required = f.required === true || (f.required === mode);
      if (required && (val === undefined || val === null || String(val).trim() === "")) v[f.name] = `${f.label} is required`;
      else if (f.validate && (val || required)) { const msg = f.validate(val, values); if (msg) v[f.name] = msg; }
    }
    setErrors(v); setServerError("");
    if (Object.keys(v).length) return;
    setSaving(true);
    try {
      await save(toPayload(values, mode));
      toast.success(`${entity} ${mode === "create" ? "created" : "updated"}`);
      onSaved();
    } catch (err) { setErrors(fieldErrors(err)); setServerError(errorMessage(err)); }
    finally { setSaving(false); }
  };

  const set = (name) => (e) => setValues((s) => ({ ...s, [name]: e.target.value }));

  return (
    <Modal title={`${mode === "create" ? "Add" : "Edit"} ${entity.toLowerCase()}`} onClose={onClose} wide>
      <form onSubmit={submit} noValidate>
        <Alert type="error">{serverError}</Alert>
        {visible.map((f) => {
          const common = { label: f.label, value: values[f.name] ?? "", onChange: set(f.name), error: errors[f.name], hint: f.hint };
          if (f.type === "textarea") return <Textarea key={f.name} {...common} />;
          if (f.type === "select") return <Select key={f.name} {...common} options={f.options} placeholder={f.placeholder} />;
          return <Input key={f.name} {...common} type={f.type || "text"} autoComplete={f.type === "password" ? "new-password" : "off"} />;
        })}
        <div className="modal-footer-inline">
          <Button variant="secondary" onClick={onClose} disabled={saving}>Cancel</Button>
          <Button type="submit" loading={saving}>{mode === "create" ? "Create" : "Save changes"}</Button>
        </div>
      </form>
    </Modal>
  );
}
