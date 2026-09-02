"use client";

import { useState } from "react";
import { ChevronDownIcon, ChevronRightIcon, DownloadIcon } from "lucide-react";
import { exportJson } from "@/lib/utils/exportJson";

export function JsonPreview({ fileName, payload, defaultOpen = false, maxHeight = 240 }) {
  const [open, setOpen] = useState(defaultOpen);

  return (
    <div className="overflow-hidden rounded border border-line">
      <div className="flex items-center justify-between bg-canvas px-3 py-2">
        <button
          type="button"
          onClick={() => setOpen((value) => !value)}
          aria-expanded={open}
          className="mono inline-flex items-center gap-1.5 text-xs font-medium text-primary hover:text-accent"
        >
          {open ? <ChevronDownIcon className="h-3.5 w-3.5" aria-hidden="true" /> : <ChevronRightIcon className="h-3.5 w-3.5" aria-hidden="true" />}
          {fileName}
        </button>
        <button
          type="button"
          onClick={() => exportJson(payload, fileName)}
          className="inline-flex items-center gap-1 text-xs font-medium text-ink-muted transition-colors duration-150 ease-out hover:text-primary"
        >
          <DownloadIcon className="h-3.5 w-3.5" aria-hidden="true" />
          Export
        </button>
      </div>
      {open && (
        <pre className="mono overflow-auto bg-white px-3 py-2.5 text-[11px] leading-relaxed text-ink-muted" style={{ maxHeight }}>
          {JSON.stringify(payload, null, 2)}
        </pre>
      )}
    </div>
  );
}
