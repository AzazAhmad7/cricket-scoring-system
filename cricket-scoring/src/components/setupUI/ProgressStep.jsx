export default function ProgressSteps({ steps, currentStep }) {
  return (
    <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
      <div className="flex items-center justify-between gap-4 overflow-x-auto">
        {steps.map((step, index) => {
          const stepNumber = index + 1;
          const active = stepNumber === currentStep;
          const completed = stepNumber < currentStep;

          return (
            <div key={step} className="flex items-center flex-1 min-w-max">
              <div className="flex items-center gap-3">
                <div
                  className={`w-10 h-10 rounded-full flex items-center justify-center font-semibold text-sm ${
                    completed
                      ? "bg-emerald-500 text-white"
                      : active
                        ? "bg-indigo-600 text-white"
                        : "bg-slate-200 text-slate-500"
                  }`}
                >
                  {completed ? "✓" : stepNumber}
                </div>
                <span
                  className={`text-sm font-medium ${
                    active
                      ? "text-indigo-600"
                      : completed
                        ? "text-emerald-600"
                        : "text-slate-500"
                  }`}
                >
                  {step}
                </span>
              </div>

              {index < steps.length - 1 && (
                <div className="flex-1 h-0.5 bg-slate-200 mx-4 min-w-[50px]" />
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}
