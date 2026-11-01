import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { describe, expect, it, vi } from "vitest";
import { AuthContext } from "../context/AuthContext";
import Login from "../pages/Login";

function setup(login) {
  render(
    <AuthContext.Provider value={{ login, sessionExpired: false }}>
      <MemoryRouter initialEntries={["/login"]}>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/dashboard" element={<div>Student home</div>} />
          <Route path="/admin" element={<div>Admin home</div>} />
        </Routes>
      </MemoryRouter>
    </AuthContext.Provider>
  );
}

describe("Login page", () => {
  it("shows validation errors and does not call the API for empty or invalid input", async () => {
    const login = vi.fn();
    setup(login);
    await userEvent.click(screen.getByRole("button", { name: /log in/i }));
    expect(await screen.findByText("Email is required")).toBeInTheDocument();
    expect(screen.getByText("Password is required")).toBeInTheDocument();
    await userEvent.type(screen.getByLabelText("Email"), "not-an-email");
    await userEvent.type(screen.getByLabelText("Password"), "x");
    await userEvent.click(screen.getByRole("button", { name: /log in/i }));
    expect(await screen.findByText("Enter a valid email address")).toBeInTheDocument();
    expect(login).not.toHaveBeenCalled();
  });

  it("shows the server error for invalid credentials", async () => {
    const login = vi.fn().mockRejectedValue({ response: { status: 401, data: { message: "Invalid email or password" } } });
    setup(login);
    await userEvent.type(screen.getByLabelText("Email"), "a@b.co");
    await userEvent.type(screen.getByLabelText("Password"), "Wrong-pass1");
    await userEvent.click(screen.getByRole("button", { name: /log in/i }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Invalid email or password");
  });

  it("redirects an admin to the admin area and a student to the dashboard", async () => {
    setup(vi.fn().mockResolvedValue({ role: "ADMIN" }));
    await userEvent.type(screen.getByLabelText("Email"), "admin@b.co");
    await userEvent.type(screen.getByLabelText("Password"), "Passw0rd!x");
    await userEvent.click(screen.getByRole("button", { name: /log in/i }));
    await waitFor(() => expect(screen.getByText("Admin home")).toBeInTheDocument());
  });
});
