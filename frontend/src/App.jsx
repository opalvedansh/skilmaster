import { Navigate, Route, Routes } from "react-router-dom";
import ProtectedRoute, { homeFor } from "./components/ProtectedRoute";
import { useAuth } from "./context/AuthContext";
import AppLayout from "./layouts/AppLayout";
import PublicLayout from "./layouts/PublicLayout";
import Landing from "./pages/Landing";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import Subjects from "./pages/Subjects";
import Skills from "./pages/Skills";
import Practice from "./pages/Practice";
import Progress from "./pages/Progress";
import Readiness from "./pages/Readiness";
import Projects from "./pages/Projects";
import Profile from "./pages/Profile";
import NotFound from "./pages/NotFound";
import AdminDashboard from "./pages/admin/AdminDashboard";
import ManageUsers from "./pages/admin/ManageUsers";
import ManageSubjects from "./pages/admin/ManageSubjects";
import ManageSkills from "./pages/admin/ManageSkills";
import ManageQuestions from "./pages/admin/ManageQuestions";

function GuestOnly({ children }) {
  const { user, loading } = useAuth();
  if (!loading && user) return <Navigate to={homeFor(user)} replace />;
  return children;
}

export default function App() {
  return (
    <Routes>
      <Route element={<PublicLayout />}>
        <Route path="/" element={<Landing />} />
        <Route path="/login" element={<GuestOnly><Login /></GuestOnly>} />
        <Route path="/register" element={<GuestOnly><Register /></GuestOnly>} />
      </Route>

      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/profile" element={<Profile />} />

          <Route element={<ProtectedRoute role="STUDENT" />}>
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/subjects" element={<Subjects />} />
            <Route path="/subjects/:subjectId/skills" element={<Skills />} />
            <Route path="/skills" element={<Skills />} />
            <Route path="/practice" element={<Practice />} />
            <Route path="/progress" element={<Progress />} />
            <Route path="/readiness" element={<Readiness />} />
            <Route path="/projects" element={<Projects />} />
          </Route>

          <Route element={<ProtectedRoute role="ADMIN" />}>
            <Route path="/admin" element={<AdminDashboard />} />
            <Route path="/admin/users" element={<ManageUsers />} />
            <Route path="/admin/subjects" element={<ManageSubjects />} />
            <Route path="/admin/skills" element={<ManageSkills />} />
            <Route path="/admin/questions" element={<ManageQuestions />} />
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}
