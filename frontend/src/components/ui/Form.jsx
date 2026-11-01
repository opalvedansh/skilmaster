import { useId } from "react";

function FieldShell({ label, error, hint, children, id }) {
  return (
    <div className={`field ${error ? "field-error" : ""}`}>
      {label && <label htmlFor={id}>{label}</label>}
      {children}
      {hint && !error && <small className="hint">{hint}</small>}
      {error && <small className="error-text" role="alert">{error}</small>}
    </div>
  );
}

export function Input({ label, error, hint, ...rest }) {
  const id = useId();
  return (
    <FieldShell id={id} label={label} error={error} hint={hint}>
      <input id={id} aria-invalid={!!error} {...rest} />
    </FieldShell>
  );
}

export function Textarea({ label, error, hint, rows = 4, ...rest }) {
  const id = useId();
  return (
    <FieldShell id={id} label={label} error={error} hint={hint}>
      <textarea id={id} rows={rows} aria-invalid={!!error} {...rest} />
    </FieldShell>
  );
}

/** options: [{ value, label }]; set placeholder to render a leading empty option. */
export function Select({ label, error, hint, options = [], placeholder, ...rest }) {
  const id = useId();
  return (
    <FieldShell id={id} label={label} error={error} hint={hint}>
      <select id={id} aria-invalid={!!error} {...rest}>
        {placeholder !== undefined && <option value="">{placeholder}</option>}
        {options.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
      </select>
    </FieldShell>
  );
}
