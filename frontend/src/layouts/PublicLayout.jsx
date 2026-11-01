import { useState } from "react";
import { Link, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { homeFor } from "../components/ProtectedRoute";
import logo from "../assets/logo.svg";

export function Brand({ to = "/" }) {
  return (
    <Link to={to} className="brand">
      <img src={logo} alt="" width="28" height="28" />
      <span>Skill<b>Mate</b></span>
    </Link>
  );
}

export default function PublicLayout() {
  const { user } = useAuth();
  const [open, setOpen] = useState(false);
  return (
    <div className="public">
      <header className="public-nav">
        <Brand />
        <button className="icon-btn nav-toggle" aria-label="Toggle menu" aria-expanded={open} onClick={() => setOpen((o) => !o)}>☰</button>
        <nav className={open ? "open" : ""} onClick={() => setOpen(false)}>
          <a href="/#features">Features</a>
          <a href="/#how-it-works">How it works</a>
          {user ? (
            <Link className="btn btn-primary btn-md" to={homeFor(user)}>Open dashboard</Link>
          ) : (
            <>
              <Link className="btn btn-secondary btn-md" to="/login">Log in</Link>
              <Link className="btn btn-primary btn-md" to="/register">Get started</Link>
            </>
          )}
        </nav>
      </header>
      <main><Outlet /></main>
      <footer className="public-footer">
        <Brand />
        <p className="muted">© {new Date().getFullYear()} SkillMate. Track skills, practise questions, prepare for placements.</p>
      </footer>
    </div>
  );
}
