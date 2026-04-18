import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { DashboardData } from '../types';
import { Activity, Target, Clock } from 'lucide-react';

export default function Dashboard({ userId }: { userId: number }) {
    const [data, setData] = useState<DashboardData | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchStats = async () => {
            try {
                const res = await axios.get(`http://localhost:8080/api/quiz/dashboard/${userId}`);
                setData(res.data);
            } catch (err) {
                console.error(err);
            } finally {
                setLoading(false);
            }
        };
        fetchStats();
    }, [userId]);

    if (loading) return <div style={{ textAlign: 'center' }}>Loading dashboard...</div>;
    if (!data) return <div style={{ textAlign: 'center' }}>Failed to load dashboard.</div>;

    return (
        <div>
            <h2 style={{ fontSize: '2rem', marginBottom: '2rem' }}>Your Reading Command Dashboard</h2>

            <div className="grid grid-2" style={{ marginBottom: '3rem' }}>
                <div className="glass-card">
                    <div className="flex-row" style={{ marginBottom: '1rem' }}>
                        <Activity color="var(--accent)" size={24} />
                        <h3 style={{ margin: 0 }}>Book Summaries</h3>
                    </div>
                    {data.bookStats.length === 0 ? <p className="text-muted">No books evaluated yet.</p> : null}
                    {data.bookStats.map((stat, i) => (
                        <div key={i} style={{ background: 'rgba(0,0,0,0.2)', padding: '1rem', borderRadius: '8px', marginBottom: '1rem' }}>
                            <div className="flex-row" style={{ justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                                <strong style={{ fontSize: '1.1rem' }}>{stat.bookName}</strong>
                                <span className="text-gradient" style={{ fontWeight: 'bold' }}>{Math.round(stat.averageScore)}% Avg</span>
                            </div>
                            <div style={{ width: '100%', height: '8px', background: 'rgba(255,255,255,0.1)', borderRadius: '4px', overflow: 'hidden' }}>
                                <div style={{ width: `${stat.averageScore}%`, height: '100%', background: 'var(--accent)' }}></div>
                            </div>
                            <p className="text-muted" style={{ fontSize: '0.875rem', marginTop: '0.5rem', marginBottom: 0 }}>
                                {stat.totalAttempts} Quizzes Taken
                            </p>
                        </div>
                    ))}
                </div>

                <div className="glass-card">
                    <div className="flex-row" style={{ marginBottom: '1rem' }}>
                        <Clock color="var(--primary)" size={24} />
                        <h3 style={{ margin: 0 }}>Recent Activity</h3>
                    </div>
                    <div className="flex-col" style={{ gap: '0' }}>
                        {data.recentHistory.length === 0 ? <p className="text-muted">No recent quizzes.</p> : null}
                        {data.recentHistory.map((hist, i) => (
                            <div key={i} className="flex-row" style={{ padding: '1rem 0', borderBottom: '1px solid rgba(255,255,255,0.1)', justifyContent: 'space-between' }}>
                                <div>
                                    <p style={{ margin: '0 0 0.25rem 0', fontWeight: 600 }}>{hist.bookName}</p>
                                    <p className="text-muted" style={{ margin: 0, fontSize: '0.875rem' }}>
                                        {new Date(hist.takenAt).toLocaleDateString()} at {new Date(hist.takenAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                    </p>
                                </div>
                                <div style={{ textAlign: 'right' }}>
                                    <p style={{ margin: '0 0 0.25rem 0', fontWeight: 'bold', color: hist.score >= 50 ? '#22c55e' : '#ef4444' }}>
                                        {hist.correctAnswers} / {hist.totalQuestions} ({Math.round(hist.score)}%)
                                    </p>
                                    <div className="flex-row" style={{ justifyContent: 'flex-end', gap: '0.25rem' }}>
                                        <Target size={14} className="text-muted" />
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
}
