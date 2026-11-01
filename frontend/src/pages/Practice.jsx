import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import AsyncBlock from "../components/AsyncBlock";
import { useToast } from "../components/Toast";
import { Badge, Button, Card, PageHeader, Spinner } from "../components/ui/Basics";
import { Input, Select } from "../components/ui/Form";
import { Modal } from "../components/ui/Modal";
import { Pagination } from "../components/ui/DataTable";
import useDebounce from "../hooks/useDebounce";
import useFetch from "../hooks/useFetch";
import { attemptsApi, questionsApi, skillsApi, subjectsApi } from "../services";
import { errorMessage } from "../services/api";
import { fmtDateTime } from "../utils/format";

const DIFFICULTIES = [{ value: "EASY", label: "Easy" }, { value: "MEDIUM", label: "Medium" }, { value: "HARD", label: "Hard" }];

export default function Practice() {
  const toast = useToast();
  const [params] = useSearchParams();
  const [filters, setFilters] = useState({ subjectId: "", skillId: params.get("skillId") || "", difficulty: "", search: "" });
  const [page, setPage] = useState(0);
  const [selected, setSelected] = useState(null);
  const search = useDebounce(filters.search, 300);

  const lookups = useFetch(() => Promise.all([subjectsApi.list(), skillsApi.list()]), []);
  const [subjects = [], skills = []] = lookups.data || [];
  const visibleSkills = skills.filter((s) => !filters.subjectId || String(s.subjectId) === filters.subjectId);

  useEffect(() => { setPage(0); }, [filters.subjectId, filters.skillId, filters.difficulty, search]);

  const list = useFetch(() => questionsApi.list({
    subjectId: filters.subjectId || undefined, skillId: filters.skillId || undefined,
    difficulty: filters.difficulty || undefined, q: search || undefined, page, size: 8,
  }), [filters.subjectId, filters.skillId, filters.difficulty, search, page]);

  const set = (k) => (e) => setFilters((f) => ({ ...f, [k]: e.target.value, ...(k === "subjectId" ? { skillId: "" } : {}) }));

  return (
    <>
      <PageHeader title="Practice questions" subtitle="Opening a question does not count as solving it. Record an attempt when you try it." />
      <Card className="filters">
        <Select label="Subject" value={filters.subjectId} onChange={set("subjectId")} placeholder="All subjects" options={subjects.map((s) => ({ value: String(s.id), label: s.name }))} />
        <Select label="Skill" value={filters.skillId} onChange={set("skillId")} placeholder="All skills" options={visibleSkills.map((s) => ({ value: String(s.id), label: s.name }))} />
        <Select label="Difficulty" value={filters.difficulty} onChange={set("difficulty")} placeholder="Any difficulty" options={DIFFICULTIES} />
        <Input label="Search" placeholder="Search question text" value={filters.search} onChange={set("search")} />
      </Card>

      <div className="mt">
        <AsyncBlock loading={list.loading} error={list.error} data={list.data} onRetry={list.reload} isEmpty={list.data?.items.length === 0}
                    emptyTitle="No questions match" emptyText="Try changing or clearing the filters.">
          <div className="stack">
            {list.data?.items.map((q) => (
              <Card key={q.id} className="question-card">
                <div className="row-between">
                  <div className="badges"><Badge value={q.difficulty} /><Badge tone="blue">{q.subjectName}</Badge><Badge tone="gray">{q.skillName}</Badge></div>
                  {q.myStatus ? <Badge value={q.myStatus} /> : <span className="muted small">Not attempted</span>}
                </div>
                <p className="question-text clamp">{q.questionText}</p>
                <Button size="sm" variant="secondary" onClick={() => setSelected(q)}>View question</Button>
              </Card>
            ))}
          </div>
          <Pagination page={page} totalPages={list.data?.totalPages || 0} onChange={setPage} />
        </AsyncBlock>
      </div>

      {selected && <QuestionModal question={selected} onClose={() => setSelected(null)}
        onRecorded={() => { list.reload(); }} toast={toast} />}
    </>
  );
}

function QuestionModal({ question, onClose, onRecorded, toast }) {
  const [saving, setSaving] = useState(null);
  const history = useFetch(() => attemptsApi.forQuestion(question.id), [question.id]);

  const record = async (status) => {
    if (saving) return;
    setSaving(status);
    try {
      await attemptsApi.record(question.id, status);
      toast.success(status === "SOLVED" ? "Marked as solved" : "Attempt recorded");
      history.reload(); onRecorded();
    } catch (err) {
      toast.error(errorMessage(err));
    } finally {
      setSaving(null);
    }
  };

  return (
    <Modal title="Question" onClose={onClose} wide
      footer={<>
        <Button variant="secondary" onClick={() => record("ATTEMPTED")} loading={saving === "ATTEMPTED"} disabled={!!saving}>Mark as attempted</Button>
        <Button onClick={() => record("SOLVED")} loading={saving === "SOLVED"} disabled={!!saving}>Mark as solved</Button>
      </>}>
      <div className="badges"><Badge value={question.difficulty} /><Badge tone="blue">{question.subjectName}</Badge><Badge tone="gray">{question.skillName}</Badge></div>
      <p className="question-full">{question.questionText}</p>
      <h4>Your previous attempts</h4>
      {history.loading && !history.data ? <Spinner /> : history.error ? <p className="error-text">{history.error}</p> :
        history.data?.length ? (
          <ul className="plain-list">{history.data.map((a) => <li key={a.id}><Badge value={a.status} /><span className="muted small">{fmtDateTime(a.attemptedAt)}</span></li>)}</ul>
        ) : <p className="muted">You have not attempted this question yet.</p>}
    </Modal>
  );
}
