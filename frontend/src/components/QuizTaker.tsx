import React, { useState } from 'react';
import axios from 'axios';
import { QuizItem } from '../types';
import { CheckCircle2, ChevronRight, XCircle } from 'lucide-react';

interface Props {
    quiz: QuizItem[];
    bookName: string;
    userId: number;
    onComplete: () => void;
}

export default function QuizTaker({ quiz, bookName, userId, onComplete }: Props) {
    const [currentIndex, setCurrentIndex] = useState(0);
    const [selectedOption, setSelectedOption] = useState<string | null>(null);
    const [answers, setAnswers] = useState<{ question: string, userAnswer: string, correctAnswer: string, isCorrect: boolean }[]>([]);
    const [isFinished, setIsFinished] = useState(false);
    const [submitting, setSubmitting] = useState(false);

    const currentQuestion = quiz[currentIndex];

    const handleNext = () => {
        if (!selectedOption) return;

        const isCorrect = selectedOption === currentQuestion.correctAnswer;
        const newAnswers = [...answers, {
            question: currentQuestion.question,
            userAnswer: selectedOption,
            correctAnswer: currentQuestion.correctAnswer,
            isCorrect
        }];
        setAnswers(newAnswers);

        if (currentIndex < quiz.length - 1) {
            setCurrentIndex(currentIndex + 1);
            setSelectedOption(null);
        } else {
            setIsFinished(true);
        }
    };

    const handleSubmit = async () => {
        setSubmitting(true);
        try {
            await axios.post('http://localhost:8080/api/quiz/submit', {
                userId,
                bookName,
                answers
            });
            onComplete(); // Back to dashboard
        } catch (e) {
            console.error(e);
            alert('Failed to submit evaluation.');
        } finally {
            setSubmitting(false);
        }
    };

    if (isFinished) {
        const score = answers.filter(a => a.isCorrect).length;
        return (
            <div className="glass-card flex-col" style={{ maxWidth: '600px', margin: '0 auto', textAlign: 'center' }}>
                <h2 style={{ fontSize: '2rem', margin: 0 }}>Evaluation Complete!</h2>
                <div style={{ padding: '2rem', background: 'rgba(255,255,255,0.05)', borderRadius: '12px' }}>
                    <p style={{ fontSize: '1.2rem', color: 'var(--text-muted)' }}>You scored</p>
                    <h1 className="text-gradient" style={{ fontSize: '4rem', margin: '0.5rem 0' }}>
                        {score} / {quiz.length}
                    </h1>
                    <p>({Math.round((score / quiz.length) * 100)}%)</p>
                </div>

                <div style={{ textAlign: 'left', marginTop: '1rem', maxHeight: '400px', overflowY: 'auto' }}>
                    {answers.map((ans, i) => (
                        <div key={i} style={{ padding: '1rem', borderBottom: '1px solid rgba(255,255,255,0.1)' }}>
                            <p style={{ fontWeight: 600 }}>{i + 1}. {ans.question}</p>
                            <div className="flex-row">
                                {ans.isCorrect ? <CheckCircle2 color="#22c55e" size={20} /> : <XCircle color="#ef4444" size={20} />}
                                <p style={{ color: ans.isCorrect ? '#22c55e' : '#ef4444', margin: 0 }}>Your answer: {ans.userAnswer}</p>
                            </div>
                            {!ans.isCorrect && <p style={{ color: '#22c55e', margin: '0.5rem 0 0 0', paddingLeft: '28px' }}>Correct: {ans.correctAnswer}</p>}
                        </div>
                    ))}
                </div>

                <button className="btn" onClick={handleSubmit} disabled={submitting} style={{ marginTop: '2rem' }}>
                    {submitting ? 'Saving...' : 'Save to Dashboard'}
                </button>
            </div>
        );
    }

    return (
        <div className="glass-card" style={{ maxWidth: '700px', margin: '0 auto' }}>
            <div className="flex-row" style={{ justifyContent: 'space-between', marginBottom: '2rem' }}>
                <span style={{ color: 'var(--accent)', fontWeight: 600 }}>Question {currentIndex + 1} of {quiz.length}</span>
                <span style={{ color: 'var(--text-muted)' }}>{bookName}</span>
            </div>

            <h3 style={{ fontSize: '1.5rem', marginBottom: '2rem' }}>{currentQuestion?.question}</h3>

            <div className="flex-col">
                {currentQuestion?.options.map((option, i) => (
                    <button
                        key={i}
                        className={`option-btn ${selectedOption === option ? 'selected' : ''}`}
                        onClick={() => setSelectedOption(option)}
                    >
                        {option}
                    </button>
                ))}
            </div>

            <div style={{ marginTop: '2rem', textAlign: 'right' }}>
                <button className="btn flex-row" onClick={handleNext} disabled={!selectedOption} style={{ display: 'inline-flex' }}>
                    {currentIndex === quiz.length - 1 ? 'Finish' : 'Next Question'} <ChevronRight size={18} />
                </button>
            </div>
        </div>
    );
}
