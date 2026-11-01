import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../components/Toast";
import { Alert, Badge, Button, Card, PageHeader } from "../components/ui/Basics";
import { Input } from "../components/ui/Form";
import { usersApi } from "../services";
import { errorMessage, fieldErrors } from "../services/api";
import { fmtDate } from "../utils/format";
import { passwordIssue } from "../utils/validators";

export default function Profile() {
  const { user, setUser } = useAuth();
  const toast = useToast();
  const [name, setName] = useState(user.name);
  const [nameError, setNameError] = useState("");
  const [savingName, setSavingName] = useState(false);
  const [pw, setPw] = useState({ currentPassword: "", password: "", confirm: "" });
  const [pwErrors, setPwErrors] = useState({});
  const [pwServerError, setPwServerError] = useState("");
  const [savingPw, setSavingPw] = useState(false);

  const saveName = async (e) => {
    e.preventDefault();
    if (savingName) return;
    if (!name.trim()) return setNameError("Name is required");
    setNameError(""); setSavingName(true);
    try { setUser(await usersApi.update(user.id, { name: name.trim() })); toast.success("Profile updated"); }
    catch (err) { setNameError(errorMessage(err)); }
    finally { setSavingName(false); }
  };

  const savePassword = async (e) => {
    e.preventDefault();
    if (savingPw) return;
    const v = {};
    if (!pw.currentPassword) v.currentPassword = "Current password is required";
    const issue = passwordIssue(pw.password);
    if (issue) v.password = issue;
    if (pw.confirm !== pw.password) v.confirm = "Passwords do not match";
    setPwErrors(v); setPwServerError("");
    if (Object.keys(v).length) return;
    setSavingPw(true);
    try {
      await usersApi.update(user.id, { currentPassword: pw.currentPassword, password: pw.password });
      toast.success("Password changed");
      setPw({ currentPassword: "", password: "", confirm: "" });
    } catch (err) { setPwErrors(fieldErrors(err)); setPwServerError(errorMessage(err)); }
    finally { setSavingPw(false); }
  };

  return (
    <>
      <PageHeader title="Profile" subtitle="Your account details" />
      <div className="grid grid-2">
        <Card title="Account">
          <dl className="details">
            <dt>Name</dt><dd>{user.name}</dd>
            <dt>Email</dt><dd>{user.email}</dd>
            <dt>Role</dt><dd><Badge value={user.role} /></dd>
            <dt>Member since</dt><dd>{fmtDate(user.createdAt)}</dd>
          </dl>
          <form onSubmit={saveName} noValidate>
            <Input label="Display name" value={name} onChange={(e) => setName(e.target.value)} error={nameError} maxLength={100} />
            <Button type="submit" loading={savingName}>Save name</Button>
          </form>
        </Card>
        <Card title="Change password">
          <Alert type="error">{pwServerError}</Alert>
          <form onSubmit={savePassword} noValidate>
            <Input label="Current password" type="password" autoComplete="current-password" value={pw.currentPassword} onChange={(e) => setPw({ ...pw, currentPassword: e.target.value })} error={pwErrors.currentPassword} />
            <Input label="New password" type="password" autoComplete="new-password" value={pw.password} onChange={(e) => setPw({ ...pw, password: e.target.value })} error={pwErrors.password} hint="At least 8 characters with a letter and a number" />
            <Input label="Confirm new password" type="password" autoComplete="new-password" value={pw.confirm} onChange={(e) => setPw({ ...pw, confirm: e.target.value })} error={pwErrors.confirm} />
            <Button type="submit" loading={savingPw}>Change password</Button>
          </form>
        </Card>
      </div>
    </>
  );
}
