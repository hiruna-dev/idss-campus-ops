import { Geist, Geist_Mono } from "next/font/google";
import Link from "next/link";
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata = {
  title: "IDSS — University Exam Operations",
  description: "Intelligent Decision Support System for Campus & Exam Operations",
};

const navItems = [
  { href: "/dashboard", label: "Dashboard" },
  { href: "/task1", label: "Task 1 — Routing" },
  { href: "/task2", label: "Task 2 — Invigilator" },
  { href: "/task3", label: "Task 3 — Clash Detection" },
  { href: "/task4", label: "Task 4 — Room Ranking" },
  { href: "/task5", label: "Task 5 — Timetable" },
];

export default function RootLayout({ children }) {
  return (
    <html
      lang="en"
      className={`${geistSans.variable} ${geistMono.variable} h-full antialiased`}
    >
      <body className="min-h-full flex flex-col bg-bg text-idss-text">
        <header className="bg-primary text-white shadow-md">
          <div className="mx-auto max-w-7xl px-4">
            <h1 className="py-3 text-lg font-bold">
              IDSS — University Exam Operations
            </h1>
            <nav className="flex flex-wrap gap-1 pb-2">
              {navItems.map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  className="rounded px-3 py-1.5 text-sm font-medium text-white/80 transition hover:bg-white/10 hover:text-white"
                >
                  {item.label}
                </Link>
              ))}
            </nav>
          </div>
        </header>
        <main className="mx-auto w-full max-w-7xl flex-1 px-4 py-6">
          {children}
        </main>
      </body>
    </html>
  );
}
