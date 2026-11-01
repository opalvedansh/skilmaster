import { useState } from "react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { Alert, Button } from "../components/ui/Basics";
import { Brand } from "./PublicLayout";

const STUDENT_LINKS = [
  ["/dashboard", "Dashboard"], ["/subjects", "Subjects"], ["/practice", "Practice"], ["/progress", "Progress"],
  ["/readiness", "Placement readiness"], ["/projects", "Projects"], ["/profile", "Profile"],
];
const ADMIN_LINKS = [
  ["/admin", "Admin dashboard"], ["/admin/users", "Users"], ["/admin/subjects", "Subjects"],
  ["/admin/skills", "Skills"], ["/admin/questions", "Questions"], ["/profile", "Profile"],
];

export default function AppLayout() {
  const { user, isAdmin, logout } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const links = isAdmin ? ADMIN_LINKS : STUDENT_LINKS;

  const onLogout = async () => { await logout(); navigate("/login", { replace: true }); };

  return (
    <div className="shell">
      <aside className={`sidebar ${open ? "open" : ""}`}>
        <Brand to={isAdmin ? "/admin" : "/dashboard"} />
        <nav aria-label="Main navigation" onClick={() => setOpen(false)}>
          {links.map(([to, text]) => (
            <NavLink key={to} to={to} end={to === "/admin"} className={({ isActive }) => (isActive ? "active" : "")}>{text}</NavLink>
          ))}
        </nav>
        <div className="sidebar-foot muted">Estimates shown in SkillMate are not a guarantee of placement.</div>
      </aside>
      {open && <div className="scrim" onClick={() => setOpen(false)} />}
      <div className="main">
        <header className="topbar">
          <button className="icon-btn menu-btn" aria-label="Open navigation" onClick={() => setOpen(true)}>☰</button>
          <div className="topbar-right">
            <span className="user-chip"><span className="avatar">{user.name?.[0]?.toUpperCase()}</span>{user.name}</span>
            <Button variant="secondary" size="sm" onClick={onLogout}>Log out</Button>
          </div>
        </header>
        <div className="content"><Outlet /></div>
      </div>
    </div>
  );
}
