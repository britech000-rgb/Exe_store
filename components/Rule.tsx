const steps = [
  "You say what you need.",
  "I check it.",
  "I confirm the price on WhatsApp before you pay.",
];

export function Rule() {
  return (
    <ol className="rule">
      {steps.map((step, index) => (
        <li key={step}>
          <span>0{index + 1}</span>
          <p>{step}</p>
        </li>
      ))}
    </ol>
  );
}
