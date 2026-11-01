import { Link } from "react-router-dom";
import AsyncBlock from "../components/AsyncBlock";
import { PercentBarChart } from "../components/charts";
import { Alert, Badge, Card, EmptyState, PageHeader, ProgressBar } from "../components/ui/Basics";
import { DataTable } from "../components/ui/DataTable";
import useFetch from "../hooks/useFetch";
import { dashboardApi } from "../services";
import { pct } from "../utils/format";

export default function Readiness() {
  const { data, loading, error, reload } = useFetch(dashboardApi.readiness, []);

  return (
    <>
      <PageHeader title="Placement readiness" subtitle="An estimate based only on the progress you have recorded" />
      <AsyncBlock loading={loading} error={error} data={data} onRetry={reload}>
        {data && (
          <>
            <Alert type="info">{data.disclaimer}</Alert>
            <div className="grid grid-2 mt">
              <Card title="Overall readiness estimate">
                {data.assessed ? (
                  <>
                    <p className="big">{pct(data.overallPercentage)} <Badge value={data.level} /></p>
                    <ProgressBar value={data.overallPercentage} showLabel={false} />
                    <p className="muted small">Based on {data.assessedSkills} started skills out of {data.totalSkills}.</p>
                  </>
                ) : <EmptyState title="Not yet assessed">Start a skill or solve a question and your estimate will appear here.</EmptyState>}
              </Card>
              <Card title="How it is calculated"><p className="muted">{data.formula}</p></Card>
            </div>

            <Card title="Subject-wise readiness" className="mt">
              {data.subjects.length ? (
                <>
                  <PercentBarChart data={data.subjects.map((s) => ({ name: s.subjectName, value: s.percentage }))} />
                  <DataTable rows={data.subjects} rowKey="subjectId" columns={[
                    { key: "subjectName", label: "Subject" },
                    { key: "percentage", label: "Readiness", render: (s) => <ProgressBar value={s.percentage} /> },
                    { key: "assessedSkills", label: "Skills started", render: (s) => `${s.assessedSkills} / ${s.skillCount}` },
                    { key: "classification", label: "Status", render: (s) => <Badge value={s.classification} /> },
                  ]} />
                </>
              ) : <EmptyState title="No subjects configured" />}
            </Card>

            <div className="grid grid-2 mt">
              <Card title="Strong areas">
                {data.strongSkills.length ? <ul className="plain-list">{data.strongSkills.map((s) => <li key={s.skillId}><span>{s.skillName} <small className="muted">{s.subjectName}</small></span><Badge tone="teal">{pct(s.percentage)}</Badge></li>)}</ul>
                  : <EmptyState title="No strong areas yet">Skills at {data.strongThreshold}% or more appear here.</EmptyState>}
              </Card>
              <Card title="Weak areas">
                {data.weakSkills.length ? <ul className="plain-list">{data.weakSkills.map((s) => <li key={s.skillId}><span>{s.skillName} <small className="muted">{s.subjectName}</small></span><Badge tone="red">{pct(s.percentage)}</Badge></li>)}</ul>
                  : <EmptyState title="No weak areas detected">Only started skills below {data.weakThreshold}% are listed. Skills you have not started are "not yet assessed".</EmptyState>}
              </Card>
            </div>

            <Card title="Recommended areas for further practice" className="mt">
              {data.recommendations.length ? (
                <ul className="plain-list">
                  {data.recommendations.map((r) => (
                    <li key={r.subjectId}><span><strong>{r.subjectName}</strong><br /><small className="muted">{r.reason}</small></span><Link to={`/subjects/${r.subjectId}/skills`}>Open skills</Link></li>
                  ))}
                </ul>
              ) : <EmptyState title="You are on track in every subject" />}
            </Card>
          </>
        )}
      </AsyncBlock>
    </>
  );
}
