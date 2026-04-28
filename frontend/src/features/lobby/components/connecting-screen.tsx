interface ConnectingScreenProps {
  attempts: number
}

const ConnectingScreen = ({ attempts }: ConnectingScreenProps) => {
  return (
    <div className="flex flex-col items-center gap-6">
      {/* Pulsing dots */}
      <div className="flex items-center gap-2">
        {[0, 1, 2].map((i) => (
          <span
            key={i}
            className="h-2.5 w-2.5 rounded-full bg-foreground opacity-80"
            style={{
              animation: "bounce 1.2s ease-in-out infinite",
              animationDelay: `${i * 0.2}s`,
            }}
          />
        ))}
      </div>

      <div className="flex flex-col items-center gap-1 text-center">
        <p className="text-sm font-semibold tracking-widest text-muted-foreground uppercase">
          Connecting
          {attempts > 1 && <>{` (Retry ${attempts - 1})`}</>}
        </p>
      </div>

      <style>
        {`
          @keyframes bounce {
            0%, 80%, 100% { transform: translateY(0); opacity: 0.3; }
            40% { transform: translateY(-0.5rem); opacity: 1; }
          }
        `}
      </style>
    </div>
  )
}

export default ConnectingScreen
