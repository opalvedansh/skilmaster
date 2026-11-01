import { useState } from "react";
import AsyncBlock from "../components/AsyncBlock";
import { useToast } from "../components/Toast";
import { Alert, Badge, Button, Card, PageHeader } from "../components/ui/Basics";
import { Input, Select, Textarea } from "../components/ui/Form";
import { ConfirmDialog, Modal } from "../components/ui/Modal";
import useFetch from "../hooks/useFetch";
import { projectsApi } from "../services";
import { errorMessage, fieldErrors } from "../services/api";
import { isGithubUrl } from "../utils/validators";

const STATUSES = [{ value: "PLANNED", label: "Planned" }, { value: "IN_PROGRESS", label: "In progress" }, { value: "COMPLETED", label: "Completed" }];
const EMPTY = { title: "", description: "", technology: "", githubUrl: "", status: "PLANNED" };

export default function Projects() {
  const toast = useToast();
  const { data, loading, error, reload } = useFetch(projectsApi.mine, []);
  const [editing, setEditing] = useState(null); // null | { id?, ...form }
  const [deleting, setDeleting] = useState(null);
  const [busy, setBusy] = useState(false);

  const remove = async () => {
    if (busy) return;
    setBusy(true);
    try { await projectsApi.remove(deleting.id); toast.success("Project deleted"); setDeleting(null); reload(); }
    catch (err) { toast.error(errorMessage(err)); }
    finally { setBusy(false); }
  };

  return (
    <>
      <PageHeader title="My projects" subtitle="Your personal project portfolio" actions={<Button onClick={() => setEditing({ ...EMPTY })}>Add project</Button>} />
      <AsyncBlock loading={loading} error={error} data={data} onRetry={reload} isEmpty={data?.length === 0}
                  emptyTitle="No projects yet" emptyText="Add your first project to start building your portfolio.">
        <div className="grid grid-2">
          {data?.map((p) => (
            <Card key={p.id}>
              <div className="row-between"><h3>{p.title}</h3><Badge value={p.status} /></div>
              <p className="muted">{p.description || "No description"}</p>
              {p.technology && <p className="small"><strong>Tech:</strong> {p.technology}</p>}
              {p.githubUrl && <p className="small"><a href={p.githubUrl} target="_blank" rel="noopener noreferrer">{p.githubUrl}</a></p>}
              <div className="row-actions">
                <Button size="sm" variant="secondary" onClick={() => setEditing({ ...p, description: p.description || "", technology: p.technology || "", githubUrl: p.githubUrl || "" })}>Edit</Button>
                <Button size="sm" variant="danger" onClick={() => setDeleting(p)}>Delete</Button>
              </div>
            </Card>
          ))}
        </div>
      </AsyncBlock>
      {editing && <ProjectForm initial={editing} onClose={() => setEditing(null)} onSaved={() => { setEditing(null); reload(); }} toast={toast} />}
      {deleting && <ConfirmDialog message={`Delete "${deleting.title}"? This cannot be undone.`} loading={busy} onConfirm={remove} onCancel={() => setDeleting(null)} />}
    </>
  );
}

function ProjectForm({ initial, onClose, onSaved, toast }) {
  const [form, setForm] = useState(initial);
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState("");
  const [saving, setSaving] = useState(false);
  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }));

  const submit = async (e) => {
    e.preventDefault();
    if (saving) return;
    const v = {};
    if (!form.title.trim()) v.title = "Title is required";
    if (!isGithubUrl(form.githubUrl)) v.githubUrl = "GitHub URL must look like https://github.com/user/repo";
    setErrors(v); setServerError("");
    if (Object.keys(v).length) return;
    setSaving(true);
    const payload = { title: form.title, description: form.description, technology: form.technology, githubUrl: form.githubUrl.trim(), status: form.status };
    try {
      if (form.id) await projectsApi.update(form.id, payload); else await projectsApi.create(payload);
      toast.success(form.id ? "Project updated" : "Project added");
      onSaved();
    } catch (err) {
      setErrors(fieldErrors(err)); setServerError(errorMessage(err));
    } finally { setSaving(false); }
  };

  return (
    <Modal title={form.id ? "Edit project" : "Add project"} onClose={onClose} wide>
      <form onSubmit={submit} noValidate>
        <Alert type="error">{serverError}</Alert>
        <Input label="Title" value={form.title} onChange={set("title")} error={errors.title} maxLength={150} />
        <Textarea label="Description" value={form.description} onChange={set("description")} error={errors.description} />
        <Input label="Technologies used" placeholder="React, Spring Boot, MySQL" value={form.technology} onChange={set("technology")} error={errors.technology} />
        <Input label="GitHub URL" placeholder="https://github.com/user/repo" value={form.githubUrl} onChange={set("githubUrl")} error={errors.githubUrl} />
        <Select label="Status" value={form.status} onChange={set("status")} options={STATUSES} />
        <div className="modal-footer-inline">
          <Button variant="secondary" onClick={onClose} disabled={saving}>Cancel</Button>
          <Button type="submit" loading={saving}>{form.id ? "Save changes" : "Add project"}</Button>
        </div>
      </form>
    </Modal>
  );
}
