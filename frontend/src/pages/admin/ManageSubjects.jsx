import ResourceManager from "../../components/admin/ResourceManager";
import { subjectsApi } from "../../services";
import { clientPage } from "../../utils/format";

export default function ManageSubjects() {
  return (
    <ResourceManager
      title="Subject management" subtitle="Subjects group related skills" entity="Subject" searchPlaceholder="Search subjects"
      fetchPage={clientPage(subjectsApi.list, (s, q) => !q || s.name.toLowerCase().includes(q) || (s.description || "").toLowerCase().includes(q))}
      columns={[
        { key: "name", label: "Name" },
        { key: "description", label: "Description", render: (s) => <span className="clamp">{s.description || "-"}</span> },
        { key: "skillCount", label: "Skills" },
      ]}
      fields={[
        { name: "name", label: "Name", required: true },
        { name: "description", label: "Description", type: "textarea" },
      ]}
      toForm={(s) => ({ name: s?.name || "", description: s?.description || "" })}
      toPayload={(v) => ({ name: v.name, description: v.description })}
      onCreate={subjectsApi.create} onUpdate={subjectsApi.update} onDelete={(s) => subjectsApi.remove(s.id)}
      deleteMessage={(s) => `Delete subject "${s.name}"? Its ${s.skillCount} skill(s), all their questions, and every student's related progress and attempts will be permanently deleted.`}
    />
  );
}
