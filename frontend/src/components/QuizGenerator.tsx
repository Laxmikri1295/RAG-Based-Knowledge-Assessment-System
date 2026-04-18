import React, { useState } from 'react';
import axios from 'axios';
import { QuizItem } from '../types';
import { BookMarked, Sparkles } from 'lucide-react';

interface Props {
    onQuizReady: (quiz: QuizItem[], bookName: string) => void;
}

export default function QuizGenerator({ onQuizReady }: Props) {
    const [bookName, setBookName] = useState('');
    const [chapter, setChapter] = useState('');
    const [questionCount, setQuestionCount] = useState(5);
    const [isGenerating, setIsGenerating] = useState(false);
    const [error, setError] = useState('');

    const handleGenerate = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!bookName) {
            setError("Please enter the book name (document ID).");
            return;
        }

        setIsGenerating(true);
        setError('');

        let instruction = `Generate ${questionCount} multiple choice questions`;
        if (chapter) {
            instruction += ` specifically focusing on ${chapter}.`;
        } else {
            instruction += ` covering the general concepts of the whole book.`;
        }

        try {
            const response = await axios.post<QuizItem[]>('http://localhost:8080/rag/structured-quiz', {
                bookName: bookName,
                instruction: instruction
            });

            // Basic validation of the response
            if (Array.isArray(response.data) && response.data.length > 0) {
                onQuizReady(response.data, bookName);
            } else {
                setError('The AI returned an empty or invalid format. Please try again.');
            }
        } catch (err) {
            console.error(err);
            setError('Failed to generate quiz. Make sure the backend and Ollama are running.');
        } finally {
            setIsGenerating(false);
        }
    };

    return (
        <div className="glass-card" style={{ maxWidth: '600px', margin: '0 auto' }}>
            <div className="flex-row" style={{ marginBottom: '1.5rem' }}>
                <BookMarked size={28} color="var(--primary)" />
                <h2 style={{ margin: 0 }}>Prepare Your Evaluation</h2>
            </div>
            <p style={{ color: 'var(--text-muted)', marginBottom: '2rem' }}>
                Generate a custom evaluation based on a specific chapter or the whole book context. Let AI formulate the questions!
            </p>

            <form onSubmit={handleGenerate} className="flex-col">
                <div>
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 500 }}>Book Name (Document ID)</label>
                    <input
                        type="text"
                        placeholder="e.g. system_design_interview"
                        value={bookName}
                        onChange={e => setBookName(e.target.value)}
                    />
                </div>

                <div>
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 500 }}>Chapter / Topic (Optional)</label>
                    <input
                        type="text"
                        placeholder="e.g. Chapter 4 OR leave blank for whole book"
                        value={chapter}
                        onChange={e => setChapter(e.target.value)}
                    />
                </div>

                <div>
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 500 }}>Number of Questions</label>
                    <input
                        type="number"
                        min="1"
                        max="20"
                        value={questionCount}
                        onChange={e => setQuestionCount(parseInt(e.target.value))}
                    />
                </div>

                <button type="submit" className="btn flex-row" style={{ justifyContent: 'center', marginTop: '1rem' }} disabled={isGenerating}>
                    {isGenerating ? 'Analyzing Text & Generating...' : <><Sparkles size={18} /> Generate Quiz</>}
                </button>
            </form>

            {error && <p style={{ color: '#ef4444', marginTop: '1rem', textAlign: 'center' }}>{error}</p>}
        </div>
    );
}
