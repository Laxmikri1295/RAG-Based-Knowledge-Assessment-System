import { useState } from 'react';
import axios from 'axios';
import { Upload, BookOpen, LayoutDashboard, BrainCircuit } from 'lucide-react';
import BookUpload from './components/BookUpload';
import QuizGenerator from './components/QuizGenerator';
import QuizTaker from './components/QuizTaker';
import Dashboard from './components/Dashboard';
import { QuizItem } from './types';

// Hardcoded userId for demo purposes. Real app would have auth.
const USER_ID = 1;

function App() {
    const [activeTab, setActiveTab] = useState<'upload' | 'generate' | 'take_quiz' | 'dashboard'>('upload');
    const [currentQuiz, setCurrentQuiz] = useState<QuizItem[]>([]);
    const [currentBook, setCurrentBook] = useState<string>('');

    const navigateToQuiz = (quiz: QuizItem[], bookName: string) => {
        setCurrentQuiz(quiz);
        setCurrentBook(bookName);
        setActiveTab('take_quiz');
    };

    const handleQuizComplete = () => {
        setActiveTab('dashboard');
    };

    return (
        <div className="app-container">
            <header className="header">
                <div className="flex-row">
                    <BrainCircuit size={32} color="var(--accent)" />
                    <h1 className="text-gradient" style={{ margin: 0 }}>Book Reader AI Evaluation</h1>
                </div>
                <nav className="nav-links">
                    <button
                        className={`nav-btn ${activeTab === 'upload' ? 'active' : ''}`}
                        onClick={() => setActiveTab('upload')}
                    >
                        <Upload size={18} style={{ marginRight: '6px', verticalAlign: 'middle' }} />
                        Upload
                    </button>
                    <button
                        className={`nav-btn ${activeTab === 'generate' ? 'active' : ''}`}
                        onClick={() => setActiveTab('generate')}
                    >
                        <BookOpen size={18} style={{ marginRight: '6px', verticalAlign: 'middle' }} />
                        Study & Quiz
                    </button>
                    {currentQuiz.length > 0 && (
                        <button
                            className={`nav-btn ${activeTab === 'take_quiz' ? 'active' : ''}`}
                            onClick={() => setActiveTab('take_quiz')}
                        >
                            Take Quiz
                        </button>
                    )}
                    <button
                        className={`nav-btn ${activeTab === 'dashboard' ? 'active' : ''}`}
                        onClick={() => setActiveTab('dashboard')}
                    >
                        <LayoutDashboard size={18} style={{ marginRight: '6px', verticalAlign: 'middle' }} />
                        Dashboard
                    </button>
                </nav>
            </header>

            <main>
                {activeTab === 'upload' && <BookUpload />}
                {activeTab === 'generate' && <QuizGenerator onQuizReady={navigateToQuiz} />}
                {activeTab === 'take_quiz' && <QuizTaker quiz={currentQuiz} bookName={currentBook} userId={USER_ID} onComplete={handleQuizComplete} />}
                {activeTab === 'dashboard' && <Dashboard userId={USER_ID} />}
            </main>
        </div>
    );
}

export default App;
