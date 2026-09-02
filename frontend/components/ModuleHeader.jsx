import { ArrowRightIcon } from "lucide-react";

export function ModuleHeader({ module, description }) {
  return (
    <header className="border-b border-line bg-white px-8 py-6">
      <div className="flex flex-wrap items-start justify-between gap-6">
        <div className="max-w-2xl">
          <p className="mono text-xs font-medium uppercase tracking-wide text-accent">Task {module.taskNumber}</p>
          <h1 className="mt-1 text-2xl font-semibold tracking-tight text-ink">{module.name}</h1>
          <p className="mt-2 text-sm leading-relaxed text-ink-muted">{description}</p>
        </div>
        <dl className="grid gap-3 text-xs sm:grid-cols-2">
          <div>
            <dt className="text-[11px] uppercase tracking-wide text-ink-faint">Algorithm</dt>
            <dd className="mt-1 font-semibold text-ink">{module.algorithm}</dd>
          </div>
          <div>
            <dt className="text-[11px] uppercase tracking-wide text-ink-faint">Gateway endpoint</dt>
            <dd className="mono mt-1 text-ink">{module.endpoint}</dd>
          </div>
        </dl>
      </div>
      <div className="mt-5 flex flex-wrap items-center gap-x-3 gap-y-2 text-[11px]">
        {module.consumes.map((file) => (
          <span key={file} className="mono rounded bg-canvas px-2 py-1 text-ink-muted">
            {file}
          </span>
        ))}
        <ArrowRightIcon className="h-3.5 w-3.5 text-ink-faint" aria-hidden="true" />
        {module.produces.map((file) => (
          <span key={file} className="mono rounded bg-primary-soft px-2 py-1 font-medium text-primary">
            {file}
          </span>
        ))}
      </div>
    </header>
  );
}
