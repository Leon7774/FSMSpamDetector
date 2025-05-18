import pandas as pd
import string
from collections import Counter
import nltk
from nltk.corpus import stopwords

# Download NLTK stopwords once
# nltk.download()

# Extend stopwords to include common Filipino words
filipino_stopwords = {
    "ang", "ng", "sa", "na", "mga", "ay", "at", "ko", "ni", "si", "ito", "yan",
    "yun", "diyan", "doon", "dito", "ikaw", "kami", "kayo", "sila", "tayo",
    "ako", "mo", "namin", "nila", "kanila", "akin", "iyo", "dapat", "hindi",
    "oo", "huwag", "wala", "may", "nasa", "kaya", "nang", "ngayon", "bakit",
    "paano", "kailan", "saan", "lahat", "iba", "kanya", "para"
}

# Combine English + Filipino stopwords
all_stopwords = set(stopwords.words('english')) | filipino_stopwords

# Load the CSV
df = pd.read_csv("spam.csv")  # replace with your filename

# Make sure 'text' column exists
if "text" not in df.columns:
    raise ValueError("The CSV file must contain a 'text' column.")

# Normalize and tokenize
words = []
for line in df["text"].dropna():
    line = line.lower().translate(str.maketrans("", "", string.punctuation))
    for word in line.split():
        if word not in all_stopwords:
            words.append(word)

# Count and print most common
top_words = Counter(words).most_common(30)
print("Most common spam-related words:")
for word, count in top_words:
    print(f"{word}: {count}")
