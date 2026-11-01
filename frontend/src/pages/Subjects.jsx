import { useNavigate } from "react-router-dom";
import AsyncBlock from "../components/AsyncBlock";
import { Card, PageHeader, ProgressBar } from "../components/ui/Basics";
import useFetch from "../hooks/useFetch";
import { subjectsApi } from "../services";

export default function Subjects() {
  const navigate = useNavigate();
  const { data, loading, error, reload } = useFetch(subjectsApi.list, []);

  return (
    <>
      <PageHeader title="Subjects" subtitle="Choose a subject to see its skills" />
      <AsyncBlock loading={loading} error={error} data={data} onRetry={reload} isEmpty={data?.length === 0}
                  emptyTitle="No subjects available" emptyText="An administrator has not added any subjects yet.">
        <div className="grid grid-3">
          {data?.map((s) => (
            <Card key={s.id} className="subject-card clickable" role="button" tabIndex={0}
                  onClick={() => navigate(`/subjects/${s.id}/skills`)}
                  onKeyDown={(e) => (e.key === "Enter" || e.key === " ") && navigate(`/subjects/${s.id}/skills`)}>
              <h3>{s.name}</h3>
              <p className="muted clamp">{s.description || "No description"}</p>
              <p className="small">{s.skillCount} {s.skillCount === 1 ? "skill" : "skills"}</p>
              <ProgressBar value={s.progressPercentage} />
            </Card>
          ))}
        </div>
      </AsyncBlock>
    </>
  );
}
