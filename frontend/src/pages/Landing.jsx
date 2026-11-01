import { Link } from "react-router-dom";
import { Card } from "../components/ui/Basics";

const FEATURES = [
  ["Skill tracking", "Break every subject into skills and track each one from not started to completed."],
  ["Practice questions", "Filter by subject, skill and difficulty. Only questions you mark as solved count as solved."],
  ["Weak area detection", "See which skills need work. Skills you have not started are never mislabelled as weak."],
  ["Readiness estimate", "A transparent formula turns your recorded progress into an estimate for DSA, DBMS, SQL and more."],
  ["Project portfolio", "Keep your projects, technologies and GitHub links organised in one place."],
  ["Admin controlled content", "Admins manage subjects, skills and questions, so the syllabus matches your placement goals."],
];
const STEPS = [
  ["1", "Create your account", "Register as a student in under a minute."],
  ["2", "Pick subjects and skills", "Start the skills you want to improve and update your status as you learn."],
  ["3", "Practise and record attempts", "Solve questions and log every attempt. Progress is calculated for you."],
  ["4", "Review your readiness", "Check strong and weak areas and follow the recommended practice areas."],
];

export default function Landing() {
  return (
    <>
      <section className="hero">
        <div className="hero-inner">
          <span className="eyebrow">Placement preparation for college students</span>
          <h1>Build skills with a plan, not a guess.</h1>
          <p>SkillMate helps you track what you know, practise what you do not, and see an honest estimate of how ready you are for placements.</p>
          <div className="hero-cta">
            <Link className="btn btn-primary btn-lg" to="/register">Create free account</Link>
            <Link className="btn btn-secondary btn-lg" to="/login">Log in</Link>
          </div>
        </div>
      </section>

      <section id="features" className="section">
        <h2>Everything you need to prepare</h2>
        <div className="grid grid-3">
          {FEATURES.map(([t, d]) => <Card key={t} className="feature"><h3>{t}</h3><p className="muted">{d}</p></Card>)}
        </div>
      </section>

      <section id="how-it-works" className="section section-alt">
        <h2>How it works</h2>
        <div className="grid grid-4">
          {STEPS.map(([n, t, d]) => (
            <div key={n} className="step">
              <span className="step-num">{n}</span>
              <h3>{t}</h3>
              <p className="muted">{d}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="section cta">
        <h2>Ready to start?</h2>
        <p className="muted">Your readiness score is an estimate based on your own recorded progress, not a promise of placement.</p>
        <Link className="btn btn-primary btn-lg" to="/register">Get started</Link>
      </section>
    </>
  );
}
