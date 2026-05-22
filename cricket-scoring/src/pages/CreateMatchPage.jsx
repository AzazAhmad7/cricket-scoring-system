import Header from "../components/setupUI/Header";
import Sidebar from "../components/setupUI/Sidebar";
import ProgressSteps from "../components/setupUI/ProgressStep";
import MatchDetailsCard from "../components/setupUI/MatchDetailCards";
import TeamsAndSquads from "../components/setupUI/TeamsAndSquad";
import TossPanel from "../components/setupUI/TossPanel";

export default function CreateMatchPage() {
  const steps = [
    "Match Details",
    "Teams & Squads",
    "Toss",
    "Review",
    "Start Match",
  ];

  return (
    <div className="min-h-screen bg-slate-100">
      <div className="flex">
        <main className="flex-1 p-6">
          <div className="max-w-7xl mx-auto space-y-6">
            <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-8">
              <div className="mb-8">
                <h1 className="text-3xl font-bold text-slate-900">
                  Create New Match
                </h1>
                <p className="text-slate-600 mt-2">
                  Configure all match settings before starting live scoring.
                </p>
              </div>

              <div className="space-y-8">
                <MatchDetailsCard />
                <TeamsAndSquads />
                <TossPanel />
              </div>

              <div className="mt-10 pt-6 border-t border-slate-200 flex justify-between">
                <button className="px-6 py-3 rounded-lg border border-slate-300 bg-white hover:bg-slate-50 font-medium">
                  Back
                </button>

                <div className="flex gap-3">
                  <button className="px-6 py-3 rounded-lg border border-slate-300 bg-white hover:bg-slate-50 font-medium">
                    Save Draft
                  </button>
                  <button className="px-8 py-3 rounded-lg bg-indigo-600 text-white hover:bg-indigo-700 font-medium shadow-sm">
                    Next Step
                  </button>
                </div>
              </div>
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}
