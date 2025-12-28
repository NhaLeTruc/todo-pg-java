import { useState, useEffect } from 'react';

interface TimerDisplayProps {
  startTime: string;
  className?: string;
}

const TimerDisplay: React.FC<TimerDisplayProps> = ({ startTime, className = '' }) => {
  const [elapsedSeconds, setElapsedSeconds] = useState(0);

  useEffect(() => {
    const calculateElapsed = () => {
      const start = new Date(startTime).getTime();
      const now = new Date().getTime();
      const elapsed = Math.floor((now - start) / 1000);
      setElapsedSeconds(elapsed);
    };

    calculateElapsed();
    const interval = setInterval(calculateElapsed, 1000);

    return () => clearInterval(interval);
  }, [startTime]);

  const formatTime = (totalSeconds: number): string => {
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;

    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
  };

  return (
    <div className={`font-mono text-lg font-semibold ${className}`}>
      {formatTime(elapsedSeconds)}
    </div>
  );
};

export default TimerDisplay;
