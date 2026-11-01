import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { errorMessage, fieldErrors } from "../services/api";
import { Alert, Button, Card } from "../components/ui/Basics";
import { Input } from "../components/ui/Form";
import { isEmail, passwordIssue } from "../utils/validators";

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: "", email: "", password: "", confirm: "" });
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }));

  const validate = () => {
    const e = {};
    if (!form.name.trim()) e.name = "Name is required";
    if (!form.email.trim()) e.email = "Email is required";
    else if (!isEmail(form.email)) e.email = "Enter a valid email address";
    const pw = passwordIssue(form.password);
    if (pw) e.password = pw;
    if (!form.confirm) e.confirm = "Please confirm your password";
    else if (form.confirm !== form.password) e.confirm = "Passwords do not match";
    return e;
  };

  const submit = async (ev) => {
    ev.preventDefault();
    if (submitting) return;
    const e = validate();
    setErrors(e); setServerError("");
    if (Object.keys(e).length) return;
    setSubmitting(true);
    try {
      // Only name, email and password are sent. The server always creates a STUDENT account.
      await register({ name: form.name.trim(), email: form.email.trim(), password: form.password });
      navigate("/dashboard", { replace: true });
    } catch (err) {
      setErrors(fieldErrors(err));
      setServerError(errorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-page">
      <Card className="auth-card">
        <h1>Create your account</h1>
        <p className="muted">Start tracking your skills today.</p>
        <Alert type="error">{serverError}</Alert>
        <form onSubmit={submit} noValidate>
          <Input label="Name" autoComplete="name" value={form.name} onChange={set("name")} error={errors.name} />
          <Input label="Email" type="email" autoComplete="email" value={form.email} onChange={set("email")} error={errors.email} />
          <Input label="Password" type="password" autoComplete="new-password" value={form.password} onChange={set("password")} error={errors.password}
                 hint="At least 8 characters with a letter and a number" />
          <Input label="Confirm password" type="password" autoComplete="new-password" value={form.confirm} onChange={set("confirm")} error={errors.confirm} />
          <Button type="submit" loading={submitting} className="btn-block">Create account</Button>
        </form>
        <p className="muted center">Already registered? <Link to="/login">Log in</Link></p>
      </Card>
    </div>
  );
}
