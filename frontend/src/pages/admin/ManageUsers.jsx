import ResourceManager from "../../components/admin/ResourceManager";
import { Badge } from "../../components/ui/Basics";
import { useAuth } from "../../context/AuthContext";
import { usersApi } from "../../services";
import { fmtDate } from "../../utils/format";
import { isEmail, passwordIssue } from "../../utils/validators";

const ROLES = [{ value: "STUDENT", label: "Student" }, { value: "ADMIN", label: "Admin" }];

export default function ManageUsers() {
  const { user: me } = useAuth();
  return (
    <ResourceManager
      title="User management" subtitle="Manage platform accounts" entity="User" searchPlaceholder="Search by name or email"
      filters={[{ name: "role", label: "Role", options: ROLES, placeholder: "All roles" }]}
      fetchPage={({ page, size, search, filters }) => usersApi.list({ page, size, q: search || undefined, role: filters.role || undefined })}
      columns={[
        { key: "name", label: "Name" }, { key: "email", label: "Email" },
        { key: "role", label: "Role", render: (u) => <Badge value={u.role} /> },
        { key: "createdAt", label: "Created", render: (u) => fmtDate(u.createdAt) },
      ]}
      fields={[
        { name: "name", label: "Name", required: true },
        { name: "email", label: "Email", required: true, validate: (v) => (isEmail(v) ? null : "Enter a valid email address") },
        { name: "role", label: "Role", type: "select", options: ROLES, required: true },
        { name: "password", label: "Password", type: "password", required: "create", hint: "Leave blank on edit to keep the current password. Change your own password on the Profile page.",
          validate: (v) => passwordIssue(v) },
      ]}
      toForm={(u) => (u ? { name: u.name, email: u.email, role: u.role, password: "" } : { name: "", email: "", role: "STUDENT", password: "" })}
      toPayload={(v, mode) => { const p = { name: v.name, email: v.email, role: v.role }; if (v.password) p.password = v.password;; return p; }}
      onCreate={usersApi.create} onUpdate={usersApi.update} onDelete={(u) => usersApi.remove(u.id)}
      canDelete={(u) => u.id !== me.id} deleteHint="You cannot delete your own account"
      deleteMessage={(u) => `Delete ${u.name} (${u.email})? Their progress, attempts and projects will be permanently removed.`}
    />
  );
}
