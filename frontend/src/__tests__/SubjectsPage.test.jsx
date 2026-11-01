import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import Subjects from "../pages/Subjects";
import { subjectsApi } from "../services";

vi.mock("../services", () => ({ subjectsApi: { list: vi.fn() } }));

const renderPage = () => render(
  <MemoryRouter initialEntries={["/subjects"]}>
    <Routes>
      <Route path="/subjects" element={<Subjects />} />
      <Route path="/subjects/:subjectId/skills" element={<div>Skills for subject</div>} />
    </Routes>
  </MemoryRouter>
);

describe("Subjects page data fetching", () => {
  beforeEach(() => vi.clearAllMocks());

  it("shows a loading indicator, then subject cards from the API", async () => {
    subjectsApi.list.mockResolvedValue([{ id: 7, name: "DSA", description: "Algorithms", skillCount: 2, progressPercentage: 40 }]);
    renderPage();
    expect(screen.getByRole("status")).toHaveTextContent("Loading");
    expect(await screen.findByText("DSA")).toBeInTheDocument();
    expect(screen.getByText("2 skills")).toBeInTheDocument();
    expect(screen.getByRole("progressbar")).toHaveAttribute("aria-valuenow", "40");
  });

  it("opens the skill list when a subject card is clicked", async () => {
    subjectsApi.list.mockResolvedValue([{ id: 7, name: "DSA", description: "", skillCount: 1, progressPercentage: 0 }]);
    renderPage();
    await userEvent.click(await screen.findByText("DSA"));
    expect(await screen.findByText("Skills for subject")).toBeInTheDocument();
  });

  it("shows an empty state when there are no subjects", async () => {
    subjectsApi.list.mockResolvedValue([]);
    renderPage();
    expect(await screen.findByText("No subjects available")).toBeInTheDocument();
  });

  it("shows the error message and retries on request", async () => {
    subjectsApi.list.mockRejectedValueOnce({ request: {} }).mockResolvedValueOnce([{ id: 1, name: "SQL", description: "", skillCount: 0, progressPercentage: 0 }]);
    renderPage();
    expect(await screen.findByRole("alert")).toHaveTextContent(/cannot reach the server/i);
    await userEvent.click(screen.getByRole("button", { name: /try again/i }));
    expect(await screen.findByText("SQL")).toBeInTheDocument();
  });
});
