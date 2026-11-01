import ResourceManager from "../../components/admin/ResourceManager";
import AsyncBlock from "../../components/AsyncBlock";
import useFetch from "../../hooks/useFetch";
import { skillsApi, subjectsApi } from "../../services";
import { clientPage } from "../../utils/format";

export default function ManageSkills() {
  const subjects = useFetch(subjectsApi.list, []);
  const options = (subjects.data || []).map((s) => ({ value: String(s.id), label: s.name }));

  return (
    <AsyncBlock loading={subjects.loading} error={subjects.error} data={subjects.data} onRetry={subjects.reload}>
      <ResourceManager
        title="Skill management" subtitle="Skills belong to exactly one subject" entity="Skill" searchPlaceholder="Search skills"
        filters={[{ name: "subjectId", label: "Subject", options, placeholder: "All subjects" }]}
        fetchPage={clientPage(skillsApi.list, (s, q, f) => (!q || s.name.toLowerCase().includes(q)) && (!f.subjectId || String(s.subjectId) === f.subjectId))}
        columns={[{ key: "name", label: "Skill" }, { key: "subjectName", label: "Subject" }, { key: "questionCount", label: "Questions" }]}
        fields={[
          { name: "name", label: "Name", required: true },
          { name: "subjectId", label: "Subject", type: "select", options, placeholder: "Select a subject", required: true },
        ]}
        toForm={(s) => ({ name: s?.name || "", subjectId: s ? String(s.subjectId) : "" })}
        toPayload={(v) => ({ name: v.name, subjectId: Number(v.subjectId) })}
        onCreate={skillsApi.create} onUpdate={skillsApi.update} onDelete={(s) => skillsApi.remove(s.id)}
        deleteMessage={(s) => `Delete skill "${s.name}"? Its ${s.questionCount} question(s) and related student progress and attempts will be permanently deleted.`}
      />
    </AsyncBlock>
  );
}
