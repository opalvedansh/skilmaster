import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { describe, expect, it, vi } from "vitest";
import { AuthContext } from "../context/AuthContext";
import Register from "../pages/Register";

const renderPage = (register) => render(
  <AuthContext.Provider value={{ register }}><MemoryRouter><Register /></MemoryRouter></AuthContext.Provider>
);

describe("Register page", () => {
  it("validates required fields, weak passwords and mismatched confirmation", async () => {
    const register = vi.fn();
    renderPage(register);
    await userEvent.click(screen.getByRole("button", { name: /create account/i }));
    expect(await screen.findByText("Name is required")).toBeInTheDocument();
    expect(screen.getByText("Email is required")).toBeInTheDocument();
    await userEvent.type(screen.getByLabelText("Name"), "Ada");
    await userEvent.type(screen.getByLabelText("Email"), "ada@example.com");
    await userEvent.type(screen.getByLabelText("Password"), "onlyletters");
    await userEvent.type(screen.getByLabelText("Confirm password"), "different1");
    await userEvent.click(screen.getByRole("button", { name: /create account/i }));
    expect(await screen.findByText(/at least one letter and one number/i)).toBeInTheDocument();
    expect(screen.getByText("Passwords do not match")).toBeInTheDocument();
    expect(register).not.toHaveBeenCalled();
  });

  it("submits only name, email and password (never a role)", async () => {
    const register = vi.fn().mockResolvedValue({ role: "STUDENT" });
    renderPage(register);
    await userEvent.type(screen.getByLabelText("Name"), "Ada");
    await userEvent.type(screen.getByLabelText("Email"), "ada@example.com");
    await userEvent.type(screen.getByLabelText("Password"), "Passw0rd!x");
    await userEvent.type(screen.getByLabelText("Confirm password"), "Passw0rd!x");
    await userEvent.click(screen.getByRole("button", { name: /create account/i }));
    expect(register).toHaveBeenCalledWith({ name: "Ada", email: "ada@example.com", password: "Passw0rd!x" });
  });
});
