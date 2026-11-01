import { Bar, BarChart, CartesianGrid, Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";

const PALETTE = ["#6c4ad0", "#2ec4b6", "#14213d", "#f4a259", "#e5566d", "#4c6ef5"];

/** Vertical or horizontal bar chart of 0-100 percentages. data: [{ name, value }] */
export function PercentBarChart({ data, horizontal = false, height = 280 }) {
  const h = horizontal ? Math.max(height, data.length * 34 + 40) : height;
  return (
    <div style={{ width: "100%", height: h }}>
      <ResponsiveContainer>
        <BarChart data={data} layout={horizontal ? "vertical" : "horizontal"} margin={{ top: 8, right: 16, left: horizontal ? 24 : 0, bottom: 8 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#e5e8ef" />
          {horizontal ? (
            <>
              <XAxis type="number" domain={[0, 100]} unit="%" />
              <YAxis type="category" dataKey="name" width={130} tick={{ fontSize: 12 }} />
            </>
          ) : (
            <>
              <XAxis dataKey="name" tick={{ fontSize: 12 }} interval={0} angle={-20} textAnchor="end" height={60} />
              <YAxis domain={[0, 100]} unit="%" />
            </>
          )}
          <Tooltip formatter={(v) => v + "%"} />
          <Bar dataKey="value" name="Progress" radius={4}>
            {data.map((d, i) => <Cell key={d.name + i} fill={d.value >= 70 ? "#2ec4b6" : d.value >= 40 ? "#6c4ad0" : "#f4a259"} />)}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}

/** data: [{ name, value }] */
export function DonutChart({ data, height = 240 }) {
  const total = data.reduce((s, d) => s + d.value, 0);
  if (!total) return <p className="muted">No data yet.</p>;
  return (
    <div style={{ width: "100%", height }}>
      <ResponsiveContainer>
        <PieChart>
          <Pie data={data} dataKey="value" nameKey="name" innerRadius={55} outerRadius={85} paddingAngle={2}>
            {data.map((d, i) => <Cell key={d.name} fill={PALETTE[i % PALETTE.length]} />)}
          </Pie>
          <Tooltip />
          <Legend />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
}
