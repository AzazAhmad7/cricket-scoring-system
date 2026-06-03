import { ArrowDownLeft, ArrowUpRight } from "lucide-react";

export function ImpactIn() {
  return (
    <span className="inline-flex items-center gap-1 px-1.5 py-0.5 rounded-md bg-emerald-50 text-emerald-600 border border-emerald-200 text-[10px] font-semibold">
      <ArrowDownLeft size={10} strokeWidth={4} />
    </span>
  );
}

export function ImpactOut() {
  return (
    <span className="inline-flex items-center gap-1 px-1.5 py-0.5 rounded-md bg-rose-50 text-rose-600 border border-rose-200 text-[10px] font-semibold">
      <ArrowUpRight size={10} strokeWidth={4} />
    </span>
  );
}
