import "./globals.css";
import { AppShell } from "@/components/AppShell";
import { PipelineProvider } from "@/contexts/PipelineContext";

export const metadata = {
  title: "IDSS — University Exam Operations",
  description: "Intelligent Decision Support System for University Campus & Exam Operations",
};

export default function RootLayout({ children }) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body className="antialiased min-h-screen bg-canvas text-ink" suppressHydrationWarning>
        <PipelineProvider>
          <AppShell>{children}</AppShell>
        </PipelineProvider>
      </body>
    </html>
  );
}
