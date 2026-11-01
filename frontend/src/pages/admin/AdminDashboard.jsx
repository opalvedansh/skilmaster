import { Link } from "react-router-dom";
import AsyncBlock from "../../components/AsyncBlock";
import { Card, PageHeader, StatCard } from "../../components/ui/Basics";
import useFetch from "../../hooks/useFetch";
import { dashboardApi } from "../../services";

const LINKS = [
  ["/admin/users", "User management", "View, edit and remove user accounts."],
  ["/admin/subjects", "Subject management", "Create and edit the subjects students study."],
  ["/admin/skills", "Skill management", "Maintain the skills inside each subject."],
  ["/admin/questions", "Question management", "Maintain the practice question bank."],
];

export default function AdminDashboard() {
  const { data, loading, error, reload } = useFetch(dashboardApi.admin, []);
  return (
    <>
      <PageHeader title="Admin dashboard" subtitle="Platform overview" />
      <AsyncBlock loading={loading} error={error} data={data} onRetry={reload}>
        {data && (
          <div className="grid grid-4">
            <StatCard label="Total users" value={data.totalUsers} hint={`${data.totalStudents} students, ${data.totalAdmins} admins`} />
            <StatCard label="Subjects" value={data.totalSubjects} tone="purple" />
            <StatCard label="Skills" value={data.totalSkills} tone="teal" />
            <StatCard label="Questions" value={data.totalQuestions} />
            <StatCard label="Question attempts" value={data.totalAttempts} tone="gray" />
          </div>
        )}
      </AsyncBlock>
      <div className="grid grid-2 mt">
        {LINKS.map(([to, title, text]) => (
          <Card key={to}><h3>{title}</h3><p className="muted">{text}</p><Link className="btn btn-secondary btn-md" to={to}>Open</Link></Card>
        ))}
      </div>
    </>
  );
}
