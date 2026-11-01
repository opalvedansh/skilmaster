import { Button } from "./Basics";

/** columns: [{ key, label, render?(row) }] */
export function DataTable({ columns, rows, rowKey = "id", actions }) {
  return (
    <div className="table-wrap">
      <table className="table">
        <thead>
          <tr>
            {columns.map((c) => <th key={c.key}>{c.label}</th>)}
            {actions && <th className="th-actions">Actions</th>}
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={row[rowKey]}>
              {columns.map((c) => <td key={c.key} data-label={c.label}>{c.render ? c.render(row) : row[c.key]}</td>)}
              {actions && <td className="td-actions">{actions(row)}</td>}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export function Pagination({ page, totalPages, onChange }) {
  if (totalPages <= 1) return null;
  return (
    <nav className="pagination" aria-label="Pagination">
      <Button variant="secondary" size="sm" disabled={page <= 0} onClick={() => onChange(page - 1)}>Previous</Button>
      <span>Page {page + 1} of {totalPages}</span>
      <Button variant="secondary" size="sm" disabled={page + 1 >= totalPages} onClick={() => onChange(page + 1)}>Next</Button>
    </nav>
  );
}
