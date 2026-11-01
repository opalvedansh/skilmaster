import { label } from "../../utils/format";

export function Button({ variant = "primary", size = "md", loading = false, disabled, children, className = "", type = "button", ...rest }) {
  return (
    <button type={type} className={`btn btn-${variant} btn-${size} ${className}`} disabled={disabled || loading} aria-busy={loading || undefined} {...rest}>
      {loading && <span className="spinner spinner-sm" aria-hidden="true" />}
      {children}
    </button>
  );
}

export function Card({ title, actions, children, className = "", ...rest }) {
  return (
    <section className={`card ${className}`} {...rest}>
      {(title || actions) && (
        <header className="card-header">
          {title && <h3>{title}</h3>}
          {actions && <div className="card-actions">{actions}</div>}
        </header>
      )}
      {children}
    </section>
  );
}

const TONES = { EASY: "teal", MEDIUM: "amber", HARD: "red", SOLVED: "teal", ATTEMPTED: "amber", COMPLETED: "teal", IN_PROGRESS: "purple",
  NOT_STARTED: "gray", PLANNED: "gray", STRONG: "teal", DEVELOPING: "amber", WEAK: "red", NOT_ASSESSED: "gray", EARLY_STAGE: "red", ADMIN: "purple", STUDENT: "blue" };

export function Badge({ value, tone, children }) {
  return <span className={`badge badge-${tone || TONES[value] || "gray"}`}>{children || label(value)}</span>;
}

export function Alert({ type = "error", children, onClose }) {
  if (!children) return null;
  return (
    <div className={`alert alert-${type}`} role={type === "error" ? "alert" : "status"}>
      <span>{children}</span>
      {onClose && <button className="alert-close" onClick={onClose} aria-label="Dismiss">×</button>}
    </div>
  );
}

export function Spinner({ text = "Loading..." }) {
  return (
    <div className="loading" role="status" aria-live="polite">
      <span className="spinner" aria-hidden="true" />
      <span>{text}</span>
    </div>
  );
}

export function ProgressBar({ value = 0, tone, showLabel = true }) {
  const v = Math.max(0, Math.min(100, Number(value) || 0));
  const auto = tone || (v >= 70 ? "teal" : v >= 40 ? "purple" : "amber");
  return (
    <div className="progress-wrap">
      <div className="progress" role="progressbar" aria-valuenow={Math.round(v)} aria-valuemin={0} aria-valuemax={100}>
        <div className={`progress-fill fill-${auto}`} style={{ width: v + "%" }} />
      </div>
      {showLabel && <span className="progress-label">{Math.round(v)}%</span>}
    </div>
  );
}

export function StatCard({ label: text, value, hint, tone = "navy" }) {
  return (
    <div className={`stat stat-${tone}`}>
      <span className="stat-label">{text}</span>
      <strong className="stat-value">{value}</strong>
      {hint && <span className="stat-hint">{hint}</span>}
    </div>
  );
}

export function EmptyState({ title = "Nothing here yet", children }) {
  return (
    <div className="empty">
      <strong>{title}</strong>
      {children && <p>{children}</p>}
    </div>
  );
}

export function PageHeader({ title, subtitle, actions }) {
  return (
    <div className="page-header">
      <div>
        <h1>{title}</h1>
        {subtitle && <p className="muted">{subtitle}</p>}
      </div>
      {actions && <div className="page-actions">{actions}</div>}
    </div>
  );
}
