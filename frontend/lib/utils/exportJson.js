/**
 * Download a JSON object as a .json file.
 * @param {object} data - The data to export
 * @param {string} filename - The filename (e.g., "output_master_schedule.json")
 */
export function exportJson(data, filename) {
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: "application/json" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}
