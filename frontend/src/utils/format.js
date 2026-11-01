export const pct = (n) => (Number(n) || 0).toFixed(1).replace(/\.0$/, "") + "%";
export const fmtDate = (d) => (d ? new Date(d).toLocaleDateString(undefined, { year: "numeric", month: "short", day: "numeric" }) : "-");
export const fmtDateTime = (d) => (d ? new Date(d).toLocaleString(undefined, { dateStyle: "medium", timeStyle: "short" }) : "-");
export const label = (s) => (s || "").replace(/_/g, " ").toLowerCase().replace(/^./, (c) => c.toUpperCase());

/** Client-side paging helper for admin lists that the API returns as plain arrays. */
export const clientPage = (listFn, match) => async ({ page, size, search, filters }) => {
  const all = await listFn();
  const q = (search || "").trim().toLowerCase();
  const rows = all.filter((i) => match(i, q, filters || {}));
  return { items: rows.slice(page * size, (page + 1) * size), totalElements: rows.length, totalPages: Math.max(1, Math.ceil(rows.length / size)) };
};
