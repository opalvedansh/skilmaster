export const isEmail = (v) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test((v || "").trim());

export function passwordIssue(pw) {
  if (!pw) return "Password is required";
  if (pw.length < 8 || pw.length > 72) return "Password must be 8-72 characters";
  if (!/[A-Za-z]/.test(pw) || !/\d/.test(pw)) return "Password must contain at least one letter and one number";
  return null;
}

export const isGithubUrl = (v) => !v || /^https:\/\/(www\.)?github\.com\/[A-Za-z0-9_.-]+(\/[A-Za-z0-9_.-]+)*\/?$/.test(v.trim());
