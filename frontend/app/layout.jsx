"use client";

import "./globals.css";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { LayoutDashboard, Route, Users, GitBranch, Building2, CalendarDays } from "lucide-react";

const navItems = [
  { href: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
  { href: "/task1", label: "Paper Routing", icon: Route },
  { href: "/task2", label: "Invigilators", icon: Users },
  { href: "/task3", label: "Clash Detection", icon: GitBranch },
  { href: "/task4", label: "Room Ranking", icon: Building2 },
  { href: "/task5", label: "Timetable", icon: CalendarDays },
];

export default function RootLayout({ children }) {
  const pathname = usePathname();

  return (
    <html lang="en" className="dark">
      <head>
        <title>IDSS — University Exam Operations</title>
        <meta name="description" content="Intelligent Decision Support System for University Campus & Exam Operations" />
      </head>
      <body className="antialiased min-h-screen flex flex-col bg-background text-foreground">
        
        {/* Top Bar Navigation */}
        <header className="sticky top-0 z-50 w-full border-b border-border bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
          <div className="container mx-auto px-4 h-14 flex items-center justify-between">
            <div className="flex items-center gap-4">
              <Link href="/dashboard" className="font-bold text-xl tracking-tight text-primary">
                IDSS <span className="text-foreground font-medium text-sm ml-2 hidden sm:inline-block">Exam Operations</span>
              </Link>
            </div>
            <nav className="flex items-center space-x-1 text-sm font-medium">
              {navItems.map((item) => {
                const isActive = pathname === item.href || (item.href !== "/dashboard" && pathname.startsWith(item.href));
                const Icon = item.icon;
                return (
                  <Link
                    key={item.href}
                    href={item.href}
                    className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md transition-all duration-200 ${
                      isActive
                        ? "bg-primary/15 text-primary font-semibold"
                        : "text-muted-foreground hover:text-foreground hover:bg-muted"
                    }`}
                  >
                    <Icon className="w-4 h-4" />
                    <span className="hidden md:inline">{item.label}</span>
                  </Link>
                );
              })}
            </nav>
          </div>
        </header>

        {/* Main Content Area */}
        <main className="flex-1 container mx-auto px-4 py-8 flex flex-col gap-8">
          {children}
        </main>

        {/* Footer */}
        <footer className="border-t border-border bg-muted/40 py-3 mt-auto">
          <div className="container mx-auto px-4 text-center text-xs text-muted-foreground">
            IDSS — Intelligent Decision Support System • University Exam Operations
          </div>
        </footer>

      </body>
    </html>
  );
}
