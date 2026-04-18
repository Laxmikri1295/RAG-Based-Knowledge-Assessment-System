export interface QuizItem {
    question: string;
    options: string[];
    correctAnswer: string;
}

export interface QuizAttempt {
    bookName: string;
    totalQuestions: number;
    correctAnswers: number;
    score: number;
    takenAt: string;
}

export interface BookStat {
    bookName: string;
    totalAttempts: number;
    averageScore: number;
}

export interface DashboardData {
    bookStats: BookStat[];
    recentHistory: QuizAttempt[];
}
