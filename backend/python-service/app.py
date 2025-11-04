from flask import Flask, request, jsonify
from flask_cors import CORS
from chatbot import FinancialChatbot
from document_processor import DocumentProcessor
import os

app = Flask(__name__)
CORS(app)

# Initialize chatbot and document processor
chatbot = FinancialChatbot()
doc_processor = DocumentProcessor()

# Configure upload folder
UPLOAD_FOLDER = 'uploads'
os.makedirs(UPLOAD_FOLDER, exist_ok=True)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER
app.config['MAX_CONTENT_LENGTH'] = 10 * 1024 * 1024  # 10MB max

@app.route('/api/health', methods=['GET'])
def health_check():
    return jsonify({
        'status': 'running',
        'service': 'Financial Assistant Python Backend',
        'ollama_available': chatbot.use_ollama,
        'huggingface_available': chatbot.use_huggingface,
        'fallback_mode': not chatbot.use_ollama and not chatbot.use_huggingface
    })

@app.route('/api/chat', methods=['POST'])
def chat():
    try:
        data = request.json
        question = data.get('question', '')
        
        if not question:
            return jsonify({'error': 'No question provided'}), 400
        
        answer = chatbot.get_response(question)
        return jsonify({'answer': answer})
    
    except Exception as e:
        print(f"Chat error: {str(e)}")
        return jsonify({'error': str(e)}), 500

@app.route('/api/document/upload', methods=['POST'])
def upload_document():
    try:
        if 'file' not in request.files:
            return jsonify({'error': 'No file provided'}), 400
        
        file = request.files['file']
        user_id = request.form.get('userId', 'default')
        
        if file.filename == '':
            return jsonify({'error': 'No file selected'}), 400
        
        # Validate file type
        allowed_extensions = {'.pdf', '.docx', '.doc', '.txt'}
        file_ext = os.path.splitext(file.filename)[1].lower()
        if file_ext not in allowed_extensions:
            return jsonify({'error': 'Unsupported file type'}), 400
        
        # Save file with safe filename
        filename = f"{user_id}_{file.filename}"
        filepath = os.path.join(app.config['UPLOAD_FOLDER'], filename)
        file.save(filepath)
        
        # Extract text from document
        try:
            text = doc_processor.extract_text(filepath)
            preview = text[:500] if text else "Could not extract text"
        except Exception as e:
            preview = f"Text extraction error: {str(e)}"
        
        return jsonify({
            'message': 'File uploaded successfully',
            'filename': filename,
            'preview': preview
        })
    
    except Exception as e:
        print(f"Upload error: {str(e)}")
        return jsonify({'error': str(e)}), 500

@app.route('/api/document/query', methods=['POST'])
def query_document():
    try:
        data = request.json
        filename = data.get('filename')
        question = data.get('question')
        
        if not filename or not question:
            return jsonify({'error': 'Missing filename or question'}), 400
        
        filepath = os.path.join(app.config['UPLOAD_FOLDER'], filename)
        
        if not os.path.exists(filepath):
            return jsonify({'error': 'Document not found'}), 404
        
        # Get document text
        doc_text = doc_processor.extract_text(filepath)
        
        # Query with context
        answer = chatbot.get_document_response(question, doc_text)
        
        return jsonify({'answer': answer})
    
    except Exception as e:
        print(f"Query error: {str(e)}")
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    print("=" * 60)
    print("🚀 Financial Assistant Python Backend")
    print("=" * 60)
    print(f"📊 Ollama Available: {chatbot.use_ollama}")
    print(f"🤗 Hugging Face Available: {chatbot.use_huggingface}")
    print(f"🔧 Fallback Mode: {not chatbot.use_ollama and not chatbot.use_huggingface}")
    print(f"📁 Upload folder: {UPLOAD_FOLDER}")
    print("=" * 60)
    print("✅ Server starting on http://localhost:5000")
    print("=" * 60)
    
    app.run(debug=True, port=5000, host='0.0.0.0')