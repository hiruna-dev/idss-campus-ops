"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { LayoutDashboardIcon, ServerIcon } from "lucide-react";
import { navModules } from "@/lib/modules";
import { usePipeline } from "@/contexts/PipelineContext";
import { GATEWAY_URL } from "@/lib/api/index";

const stageLabels = {
  1: "Stage 1 — Independent",
  2: "Stage 2 — Needs 3 + 4",
  3: "Stage 3 — Needs 5",
  4: "Stage 4 — Needs 5",
};

export function AppShell({ children }) {
  const { runs } = usePipeline();
  const pathname = usePathname();

  let lastStage = 0;

  return (
    <div className="flex min-h-screen w-full bg-canvas">
      <aside className="hidden w-64 shrink-0 flex-col bg-primary lg:flex">
        <div className="border-b border-white/10 px-5 py-5">
          <p className="text-sm font-semibold tracking-tight text-white">IDSS</p>
          <p className="mt-0.5 text-xs leading-snug text-white/60">University Campus &amp; Exam Operations</p>
        </div>

        <nav className="flex-1 overflow-y-auto px-3 py-4" aria-label="Modules">
          <Link
            href="/dashboard"
            className={`flex items-center gap-2.5 rounded px-3 py-2 text-sm font-medium transition-colors duration-150 ease-out ${
              pathname === "/dashboard" ? "bg-white/15 text-white" : "text-white/70 hover:bg-white/10"
            }`}
          >
            <LayoutDashboardIcon className="h-4 w-4" aria-hidden="true" />
            Dashboard
          </Link>

          <ul className="mt-4 space-y-0.5">
            {navModules.map((module) => {
              const showStage = module.pipelineOrder !== lastStage;
              lastStage = module.pipelineOrder;
              const state = runs[module.id].state;
              const isActive = pathname === module.path || pathname.startsWith(`${module.path}/`);
              return (
                <li key={module.id}>
                  {showStage && (
                    <p className="px-3 pb-1.5 pt-4 text-[10px] font-semibold uppercase tracking-wider text-white/40">
                      {stageLabels[module.pipelineOrder]}
                    </p>
                  )}
                  <Link
                    href={module.path}
                    className={`flex items-center gap-2.5 rounded px-3 py-2 text-sm transition-colors duration-150 ease-out ${
                      isActive ? "bg-white/15 font-semibold text-white" : "text-white/70 hover:bg-white/10"
                    }`}
                  >
                    <span className="mono w-6 shrink-0 text-xs text-white/50">T{module.taskNumber}</span>
                    <span className="flex-1 truncate">{module.shortName}</span>
                    <span
                      aria-hidden="true"
                      className={`h-1.5 w-1.5 rounded-full ${
                        state === "complete" ? "bg-emerald-400" : state === "running" ? "animate-pulse bg-amber-300" : "bg-white/25"
                      }`}
                    />
                  </Link>
                </li>
              );
            })}
          </ul>
        </nav>

        <div className="border-t border-white/10 px-5 py-4">
          <p className="flex items-center gap-2 text-[11px] text-white/60">
            <ServerIcon className="h-3.5 w-3.5" aria-hidden="true" />
            <span className="mono truncate">{GATEWAY_URL}</span>
          </p>
        </div>
      </aside>

      <div className="flex min-w-0 flex-1 flex-col">
        <div className="flex items-center gap-4 overflow-x-auto border-b border-line bg-white px-4 py-2 lg:hidden">
          <Link href="/dashboard" className="text-sm font-semibold text-primary">
            Dashboard
          </Link>
          {navModules.map((module) => (
            <Link
              key={module.id}
              href={module.path}
              className={`whitespace-nowrap text-sm ${pathname === module.path ? "font-semibold text-primary" : "text-ink-muted"}`}
            >
              T{module.taskNumber} {module.shortName}
            </Link>
          ))}
        </div>
        <main className="flex-1">{children}</main>
      </div>
    </div>
  );
}
