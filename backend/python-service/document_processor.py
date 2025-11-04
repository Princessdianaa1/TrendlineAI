import PyPDF2
from docx import Document
import os

class DocumentProcessor:
    def extract_text(self, filepath: str) -> str:
        """Extract text from PDF, DOCX, or TXT files"""
        
        file_extension = os.path.splitext(filepath)[1].lower()
        
        try:
            if file_extension == '.pdf':
                return self._extract_pdf(filepath)
            elif file_extension in ['.docx', '.doc']:
                return self._extract_docx(filepath)
            elif file_extension == '.txt':
                return self._extract_txt(filepath)
            else:
                raise ValueError(f"Unsupported file type: {file_extension}")
        except Exception as e:
            raise Exception(f"Error processing document: {str(e)}")
    
    def _extract_pdf(self, filepath: str) -> str:
        """Extract text from PDF"""
        text = ""
        try:
            with open(filepath, 'rb') as file:
                pdf_reader = PyPDF2.PdfReader(file)
                for page in pdf_reader.pages:
                    page_text = page.extract_text()
                    if page_text:
                        text += page_text + "\n"
            
            if not text.strip():
                raise Exception("No text could be extracted from PDF")
            
            return text
        except Exception as e:
            raise Exception(f"Error reading PDF: {str(e)}")
    
    def _extract_docx(self, filepath: str) -> str:
        """Extract text from DOCX"""
        try:
            doc = Document(filepath)
            paragraphs = [paragraph.text for paragraph in doc.paragraphs]
            text = "\n".join(paragraphs)
            
            if not text.strip():
                raise Exception("No text could be extracted from DOCX")
            
            return text
        except Exception as e:
            raise Exception(f"Error reading DOCX: {str(e)}")
    
    def _extract_txt(self, filepath: str) -> str:
        """Extract text from TXT"""
        try:
            with open(filepath, 'r', encoding='utf-8') as file:
                text = file.read()
            
            if not text.strip():
                raise Exception("File is empty")
            
            return text
        except UnicodeDecodeError:
            # Try with different encoding
            try:
                with open(filepath, 'r', encoding='latin-1') as file:
                    text = file.read()
                return text
            except Exception as e:
                raise Exception(f"Error reading TXT with latin-1: {str(e)}")
        except Exception as e:
            raise Exception(f"Error reading TXT: {str(e)}")