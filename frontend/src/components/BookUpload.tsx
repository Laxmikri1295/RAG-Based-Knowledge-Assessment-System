import React, { useState } from 'react';
import axios from 'axios';
import { UploadCloud } from 'lucide-react';

export default function BookUpload() {
    const [file, setFile] = useState<File | null>(null);
    const [isUploading, setIsUploading] = useState(false);
    const [message, setMessage] = useState('');

    const handleUpload = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!file) return;

        setIsUploading(true);
        setMessage('');

        const formData = new FormData();
        formData.append('file', file);
        formData.append('bookName', file.name.replace('.pdf', ''));
        formData.append('authorName', 'Unknown');

        try {
            await axios.post('http://localhost:8080/file/upload', formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });
            setMessage('Book uploaded and indexed successfully!');
            setFile(null);
        } catch (error) {
            console.error(error);
            setMessage('Failed to upload book. Ensure backend is running.');
        } finally {
            setIsUploading(false);
        }
    };

    return (
        <div className="glass-card" style={{ maxWidth: '600px', margin: '0 auto', textAlign: 'center' }}>
            <h2>Upload Book to Evaluation System</h2>
            <p className="text-muted">Upload a PDF book to analyze your understanding later.</p>

            <form onSubmit={handleUpload} className="flex-col" style={{ marginTop: '2rem' }}>
                <div style={{ padding: '2rem', border: '2px dashed rgba(255,255,255,0.2)', borderRadius: '12px', background: 'rgba(0,0,0,0.2)' }}>
                    <UploadCloud size={48} color="var(--accent)" style={{ marginBottom: '1rem' }} />
                    <input
                        type="file"
                        accept="application/pdf"
                        onChange={e => setFile(e.target.files?.[0] || null)}
                        style={{ display: 'block', margin: '0 auto' }}
                    />
                </div>

                <button type="submit" className="btn" disabled={!file || isUploading}>
                    {isUploading ? 'Processing & Vectorizing...' : 'Upload & Index Book'}
                </button>
            </form>

            {message && <p style={{ marginTop: '1rem', color: message.includes('success') ? '#22c55e' : '#ef4444' }}>{message}</p>}
        </div>
    );
}
