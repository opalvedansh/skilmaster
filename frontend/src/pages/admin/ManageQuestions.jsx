import ResourceManager from "../../components/admin/ResourceManager";
import AsyncBlock from "../../components/AsyncBlock";
import { Badge } from "../../components/ui/Basics";
import useFetch from "../../hooks/useFetch";
import { questionsApi, skillsApi, subjectsApi } from "../../services";

const DIFFICULTIES = [{ value: "EASY", label: "Easy" }, { value: "MEDIUM", label: "Medium" }, { value: "HARD", label: "Hard" }];

export default function ManageQuestions() {
  const lookups = useFetch(() => Promise.all([subjectsApi.list(), skillsApi.list()]), []);
  const [subjects = [], skills = []] = lookups.data || [];
  const subjectOptions = subjects.map((s) => ({ value: String(s.id), label: s.name }));
  const skillOptions = skills.map((s) => ({ value: String(s.id), label: `${s.subjectName} / ${s.name}` }));

  return (
    <AsyncBlock loading={lookups.loading} error={lookups.error} data={lookups.data} onRetry={lookups.reload}>
      <ResourceManager
        title="Question management" subtitle="The practice question bank" entity="Question" searchPlaceholder="Search question text"
        filters={[
          { name: "subjectId", label: "Subject", options: subjectOptions, placeholder: "All subjects" },
          { name: "difficulty", label: "Difficulty", options: DIFFICULTIES, placeholder: "Any difficulty" },
        ]}
        fetchPage={({ page, size, search, filters }) => questionsApi.list({ page, size, q: search || undefined, subjectId: filters.subjectId || undefined, difficulty: filters.difficulty || undefined })}
        columns={[
          { key: "questionText", label: "Question", render: (q) => <span className="clamp">{q.questionText}</span> },
          { key: "difficulty", label: "Difficulty", render: (q) => <Badge value={q.difficulty} /> },
          { key: "skillName", label: "Skill" }, { key: "subjectName", label: "Subject" },
        ]}
        fields={[
          { name: "questionText", label: "Question text", type: "textarea", required: true },
          { name: "difficulty", label: "Difficulty", type: "select", options: DIFFICULTIES, required: true },
          { name: "skillId", label: "Skill", type: "select", options: skillOptions, placeholder: "Select a skill", required: true },
        ]}
        toForm={(q) => ({ questionText: q?.questionText || "", difficulty: q?.difficulty || "EASY", skillId: q ? String(q.skillId) : "" })}
        toPayload={(v) => ({ questionText: v.questionText, difficulty: v.difficulty, skillId: Number(v.skillId) })}
        onCreate={questionsApi.create} onUpdate={questionsApi.update} onDelete={(q) => questionsApi.remove(q.id)}
        deleteMessage={() => "Delete this question? Students' recorded attempts for it will also be removed."}
      />
    </AsyncBlock>
  );
}
