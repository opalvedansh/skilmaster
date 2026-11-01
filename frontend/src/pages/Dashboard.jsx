import { Link } from "react-router-dom";
import AsyncBlock from "../components/AsyncBlock";
import { PercentBarChart } from "../components/charts";
import { Badge, Card, EmptyState, PageHeader, ProgressBar, StatCard } from "../components/ui/Basics";
import { DataTable } from "../components/ui/DataTable";
import useFetch from "../hooks/useFetch";
import { dashboardApi } from "../services";
import { fmtDateTime, pct } from "../utils/format";

export default function Dashboard() {
  const { data, loading, error, reload } = useFetch(() => Promise.all([dashboardApi.me(), dashboardApi.progress()]), []);
  const [summary, overview] = data || [];

  return (
    <>
      <PageHeader title={summary ? `Welcome back, ${summary.name}` : "Dashboard"} subtitle="Your progress at a glance" />
      <AsyncBlock loading={loading} error={error} data={data} onRetry={reload}>
        {summary && (
          <>
            <div className="grid grid-4">
              <StatCard label="Subjects" value={summary.totalSubjects} />
              <StatCard label="Skills" value={summary.totalSkills} tone="purple" />
              <StatCard label="Completed skills" value={summary.completedSkills} tone="teal" hint={`${summary.inProgressSkills} in progress`} />
              <StatCard label="Questions attempted" value={summary.questionsAttempted} hint={`${summary.questionsSolved} solved`} />
            </div>

            <div className="grid grid-2 mt">
              <Card title="Overall skill progress">
                <p className="big">{pct(summary.overallProgress)}</p>
                <ProgressBar value={summary.overallProgress} showLabel={false} />
                <p className="muted small">Average across all skills; skills you have not started count as 0%.</p>
              </Card>
              <Card title="Placement readiness (estimate)" actions={<Link to="/readiness">Details</Link>}>
                {summary.readinessAssessed ? (
                  <>
                    <p className="big">{pct(summary.readinessPercentage)} <Badge value={summary.readinessLevel} /></p>
                    <ProgressBar value={summary.readinessPercentage} showLabel={false} />
                  </>
                ) : (
                  <EmptyState title="Not yet assessed">Start a skill or solve a question to get your first estimate.</EmptyState>
                )}
                <p className="muted small">An estimate from your recorded progress, not a guarantee of placement.</p>
              </Card>
            </div>

            <div className="grid grid-2 mt">
              <Card title="Progress by subject">
                {overview.subjects.length ? <PercentBarChart data={overview.subjects.map((s) => ({ name: s.subjectName, value: s.percentage }))} /> : <EmptyState title="No subjects yet" />}
              </Card>
              <Card title="Progress by skill">
                {overview.skills.length ? <PercentBarChart horizontal data={overview.skills.map((s) => ({ name: s.name, value: s.percentage }))} height={280} /> : <EmptyState title="No skills yet" />}
              </Card>
            </div>

            <div className="grid grid-2 mt">
              <Card title="Strongest skills">
                {summary.strongestSkills.length ? (
                  <ul className="plain-list">{summary.strongestSkills.map((s) => <li key={s.skillId}><span>{s.skillName} <small className="muted">{s.subjectName}</small></span><Badge tone="teal">{pct(s.percentage)}</Badge></li>)}</ul>
                ) : <EmptyState title="No strong skills yet">Skills at 70% or more appear here.</EmptyState>}
              </Card>
              <Card title="Skills requiring improvement">
                {summary.skillsToImprove.length ? (
                  <ul className="plain-list">{summary.skillsToImprove.map((s) => <li key={s.skillId}><span>{s.skillName} <small className="muted">{s.subjectName}</small></span><Badge tone="red">{pct(s.percentage)}</Badge></li>)}</ul>
                ) : <EmptyState title="Nothing flagged">Only skills you have started and that are below 40% are listed.</EmptyState>}
              </Card>
            </div>

            <Card title="Recent question attempts" className="mt" actions={<Link to="/practice">Practice</Link>}>
              {summary.recentAttempts.length ? (
                <DataTable rows={summary.recentAttempts} columns={[
                  { key: "questionText", label: "Question", render: (a) => <span className="clamp">{a.questionText}</span> },
                  { key: "skillName", label: "Skill" },
                  { key: "difficulty", label: "Difficulty", render: (a) => <Badge value={a.difficulty} /> },
                  { key: "status", label: "Result", render: (a) => <Badge value={a.status} /> },
                  { key: "attemptedAt", label: "When", render: (a) => fmtDateTime(a.attemptedAt) },
                ]} />
              ) : <EmptyState title="No attempts yet">Open the practice page and record your first attempt.</EmptyState>}
            </Card>
          </>
        )}
      </AsyncBlock>
    </>
  );
}
