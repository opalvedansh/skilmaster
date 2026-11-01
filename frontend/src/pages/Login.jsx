import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { errorMessage } from "../services/api";
import { Alert, Button, Card } from "../components/ui/Basics";
import { Input } from "../components/ui/Form";
import { isEmail } from "../utils/validators";
import { homeFor } from "../components/ProtectedRoute";

export default function Login() {
  const { login, sessionExpired } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ email: "", password: "" });
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }));

  const submit = async (e) => {
    e.preventDefault();
    if (submitting) return;
    const next = {};
    if (!form.email.trim()) next.email = "Email is required";
    else if (!isEmail(form.email)) next.email = "Enter a valid email address";
    if (!form.password) next.password = "Password is required";
    setErrors(next);
    setServerError("");
    if (Object.keys(next).length) return;

    setSubmitting(true);
    try {
      const user = await login(form.email.trim(), form.password);
      const from = location.state?.from;
      navigate(from && user.role === "STUDENT" ? from : homeFor(user), { replace: true });
    } catch (err) {
      setServerError(errorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-page">
      <Card className="auth-card">
        <h1>Welcome back</h1>
        <p className="muted">Log in to continue your preparation.</p>
        {sessionExpired && <Alert type="info">Your session has expired. Please log in again.</Alert>}
        <Alert type="error">{serverError}</Alert>
        <form onSubmit={submit} noValidate>
          <Input label="Email" type="email" autoComplete="email" value={form.email} onChange={set("email")} error={errors.email} />
          <Input label="Password" type="password" autoComplete="current-password" value={form.password} onChange={set("password")} error={errors.password} />
          <Button type="submit" loading={submitting} className="btn-block">Log in</Button>
        </form>
        <p className="muted center">New to SkillMate? <Link to="/register">Create an account</Link></p>
      </Card>
    </div>
  );
}
