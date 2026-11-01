import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import AsyncBlock from "../components/AsyncBlock";
import { useToast } from "../components/Toast";
import { Badge, Card, PageHeader, ProgressBar } from "../components/ui/Basics";
import { Select } from "../components/ui/Form";
import useFetch from "../hooks/useFetch";
import { progressApi, skillsApi, subjectsApi } from "../services";
import { errorMessage } from "../services/api";

const STATUS_OPTIONS = [
  { value: "NOT_STARTED", label: "Not started" }, { value: "IN_PROGRESS", label: "In progress" }, { value: "COMPLETED", label: "Completed" },
];

export default function Skills() {
  const { subjectId } = useParams();
  const toast = useToast();
  const [updating, setUpdating] = useState(null);
  const { data, loading, error, reload } = useFetch(
    () => Promise.all([subjectId ? subjectsApi.skills(subjectId) : skillsApi.list(), subjectId ? subjectsApi.get(subjectId) : Promise.resolve(null)]),
    [subjectId]
  );
  const [skills, subject] = data || [];

  const changeStatus = async (skill, status) => {
    if (updating) return;
    setUpdating(skill.id);
    try {
      await progressApi.setStatus(skill.id, status);
      toast.success(`"${skill.name}" marked as ${status.replace("_", " ").toLowerCase()}`);
      reload();
    } catch (err) {
      toast.error(errorMessage(err));
    } finally {
      setUpdating(null);
    }
  };

  return (
    <>
      <PageHeader title={subject ? `${subject.name} skills` : "All skills"}
        subtitle={subject?.description || "Update your status as you learn. Progress is calculated from your status and solved questions."}
        actions={<Link className="btn btn-secondary btn-md" to="/subjects">All subjects</Link>} />
      <AsyncBlock loading={loading} error={error} data={data} onRetry={reload} isEmpty={skills?.length === 0}
                  emptyTitle="No skills yet" emptyText="This subject does not have any skills.">
        <div className="grid grid-2">
          {skills?.map((s) => (
            <Card key={s.id}>
              <div className="row-between">
                <div>
                  <h3>{s.name}</h3>
                  <p className="muted small">{s.subjectName} · {s.questionCount} {s.questionCount === 1 ? "question" : "questions"}</p>
                </div>
                <Badge value={s.status} />
              </div>
              <ProgressBar value={s.percentage} />
              <div className="row-between mt-sm">
                <Select aria-label={`Status for ${s.name}`} value={s.status} disabled={updating === s.id}
                        onChange={(e) => changeStatus(s, e.target.value)} options={STATUS_OPTIONS} />
                <Link to={`/practice?skillId=${s.id}`}>Practice questions</Link>
              </div>
            </Card>
          ))}
        </div>
      </AsyncBlock>
    </>
  );
}
