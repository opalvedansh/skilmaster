import { Alert, EmptyState, Spinner, Button } from "./ui/Basics";

/** Standard loading / error / empty handling for data-driven sections. */
export default function AsyncBlock({ loading, error, data, onRetry, isEmpty, emptyTitle, emptyText, children }) {
  if (loading && !data) return <Spinner />;
  if (error) {
    return (
      <div className="state-block">
        <Alert type="error">{error}</Alert>
        {onRetry && <Button variant="secondary" onClick={onRetry}>Try again</Button>}
      </div>
    );
  }
  if (!data) return null;
  if (isEmpty) return <EmptyState title={emptyTitle}>{emptyText}</EmptyState>;
  return children;
}
