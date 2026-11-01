import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { describe, expect, it } from "vitest";
import { AuthContext } from "../context/AuthContext";
import ProtectedRoute from "../components/ProtectedRoute";

function renderAt(path, auth) {
  render(
    <AuthContext.Provider value={auth}>
      <MemoryRouter initialEntries={[path]}>
        <Routes>
          <Route path="/login" element={<div>Login screen</div>} />
          <Route path="/dashboard" element={<div>Student dashboard</div>} />
          <Route path="/admin" element={<div>Admin area</div>} />
          <Route element={<ProtectedRoute role="ADMIN" />}><Route path="/admin-only" element={<div>Secret admin page</div>} /></Route>
          <Route element={<ProtectedRoute />}><Route path="/private" element={<div>Private page</div>} /></Route>
        </Routes>
      </MemoryRouter>
    </AuthContext.Provider>
  );
}

describe("ProtectedRoute navigation", () => {
  it("redirects anonymous visitors to login", () => {
    renderAt("/private", { user: null, loading: false });
    expect(screen.getByText("Login screen")).toBeInTheDocument();
  });
  it("shows a loading state while the session is being restored", () => {
    renderAt("/private", { user: null, loading: true });
    expect(screen.getByRole("status")).toHaveTextContent(/checking session/i);
  });
  it("lets authenticated users through", () => {
    renderAt("/private", { user: { role: "STUDENT" }, loading: false });
    expect(screen.getByText("Private page")).toBeInTheDocument();
  });
  it("sends a student away from admin-only routes", () => {
    renderAt("/admin-only", { user: { role: "STUDENT" }, loading: false });
    expect(screen.getByText("Student dashboard")).toBeInTheDocument();
    expect(screen.queryByText("Secret admin page")).not.toBeInTheDocument();
  });
  it("allows admins into admin-only routes", () => {
    renderAt("/admin-only", { user: { role: "ADMIN" }, loading: false });
    expect(screen.getByText("Secret admin page")).toBeInTheDocument();
  });
});
