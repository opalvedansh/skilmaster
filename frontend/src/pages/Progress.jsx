import AsyncBlock from "../components/AsyncBlock";
import { DonutChart, PercentBarChart } from "../components/charts";
import { Badge, Card, EmptyState, PageHeader, ProgressBar, StatCard } from "../components/ui/Basics";
import useFetch from "../hooks/useFetch";
import { dashboardApi } from "../services";
import { pct } from "../utils/format";

export default function Progress() {
  const { data, loading, error, reload } = useFetch(dashboardApi.progress, []);
  const by = (status) => data?.skills.filter((s) => s.status === status) || [];

  return (
    <>
      <PageHeader title="Progress" subtitle="Calculated from your saved skill progress and question attempts" />
      <AsyncBlock loading={loading} error={error} data={data} onRetry={reload}>
        {data && (
          <>
            <Card title="Overall progress">
              <p className="big">{pct(data.overallProgress)}</p>
              <ProgressBar value={data.overallProgress} showLabel={false} />
            </Card>

            <div className="grid grid-4 mt">
              <StatCard label="Completed skills" value={data.completedSkills} tone="teal" />
              <StatCard label="In progress" value={data.inProgressSkills} tone="purple" />
              <StatCard label="Pending" value={data.pendingSkills} tone="gray" />
              <StatCard label="Solve rate" value={pct(data.attemptStats.solveRate)} hint={`${data.attemptStats.questionsSolved} of ${data.attemptStats.totalQuestions} questions`} />
            </div>

            <div className="grid grid-2 mt">
              <Card title="Subject-wise progress">
                {data.subjects.length ? <PercentBarChart data={data.subjects.map((s) => ({ name: s.subjectName, value: s.percentage }))} /> : <EmptyState title="No subjects" />}
              </Card>
              <Card title="Skill status">
                <DonutChart data={[{ name: "Completed", value: data.completedSkills }, { name: "In progress", value: data.inProgressSkills }, { name: "Pending", value: data.pendingSkills }]} />
              </Card>
            </div>

            <Card title="Skill-wise progress" className="mt">
              {data.skills.length ? (
                <div className="stack">
                  {data.skills.map((s) => (
                    <div key={s.id} className="row-between skill-row">
                      <div><strong>{s.name}</strong> <span className="muted small">{s.subjectName}</span></div>
                      <Badge value={s.status} />
                      <div className="grow"><ProgressBar value={s.percentage} /></div>
                    </div>
                  ))}
                </div>
              ) : <EmptyState title="No skills yet" />}
            </Card>

            <div className="grid grid-3 mt">
              {[["Completed skills", "COMPLETED"], ["Skills in progress", "IN_PROGRESS"], ["Pending skills", "NOT_STARTED"]].map(([title, st]) => (
                <Card key={st} title={`${title} (${by(st).length})`}>
                  {by(st).length ? <ul className="plain-list">{by(st).map((s) => <li key={s.id}>{s.name}<small className="muted">{s.subjectName}</small></li>)}</ul> : <p className="muted">None</p>}
                </Card>
              ))}
            </div>

            <Card title="Question attempt statistics" className="mt">
              <div className="grid grid-4">
                <StatCard label="Total attempts" value={data.attemptStats.totalAttempts} />
                <StatCard label="Questions attempted" value={data.attemptStats.questionsAttempted} tone="purple" />
                <StatCard label="Questions solved" value={data.attemptStats.questionsSolved} tone="teal" />
                <StatCard label="Questions available" value={data.attemptStats.totalQuestions} tone="gray" />
              </div>
            </Card>
          </>
        )}
      </AsyncBlock>
    </>
  );
}
