# CHATBOT NHA KHOA - HUONG DAN SU DUNG

## TONG QUAN

Chatbot DenTeeth la tro ly ao AI su dung Gemini de:

- Tu van trieu chung rang mieng (DETERMINISTIC - cung trieu chung = cung ket qua)
- Tra cuu bang gia tu database (DYNAMIC - cap nhat tu DB)
- Tra loi FAQ (gio lam viec, dia chi, dat lich)
- Tu choi cau hoi ngoai pham vi nha khoa (OUT_OF_SCOPE)

## API ENDPOINT

### POST /api/v1/chatbot/chat

**URL Production**: `https://pdcms.duckdns.org/api/v1/chatbot/chat`

**Headers**:

```
Content-Type: application/json
```

> KHONG can Authorization - Public endpoint

**Request Body**:

```json
{
  "message": "cau hoi cua nguoi dung"
}
```

**Response**:

```json
{
  "response": "Cau tra loi tu chatbot..."
}
```

---

## CAC LOAI CAU HOI HO TRO

### 1. TU VAN TRIEU CHUNG (Deterministic Response)

Cung mot trieu chung se LUON cho cung mot ket qua - dam bao nhat quan ve y khoa.

| Symptom ID                | Keyword Vi Du                   | Mo Ta                  |
| ------------------------- | ------------------------------- | ---------------------- |
| `SYMPTOM_TOOTHACHE`       | "dau rang", "nhuc rang"         | Dau rang, nhuc rang    |
| `SYMPTOM_BLEEDING_GUMS`   | "chay mau nuou", "chay mau loi" | Chay mau nuou          |
| `SYMPTOM_LOOSE_TOOTH`     | "rang lung lay", "rang long"    | Rang lung lay          |
| `SYMPTOM_BAD_BREATH`      | "hoi mieng", "mieng hoi"        | Hoi mieng              |
| `SYMPTOM_SENSITIVE_TEETH` | "e buot", "rang nhay cam"       | E buot rang            |
| `SYMPTOM_SWOLLEN_FACE`    | "sung ma", "sung mat"           | Sung ma/mat (KHAN CAP) |
| `SYMPTOM_WISDOM_TOOTH`    | "rang khon", "rang so 8"        | Van de rang khon       |

**Vi Du Request**:

```bash
curl -X POST https://pdcms.duckdns.org/api/v1/chatbot/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "toi bi dau rang qua"}'
```

**Response**:

```json
{
  "response": "[TRIEU CHUNG DAU RANG]\n\nDua tren trieu chung, co the la mot trong cac van de sau:\n\n1. Sau rang (Dental Caries) - Pho bien nhat\n   - Dau khi an do ngot, nong, lanh\n   - Co the thay lo den tren rang\n\n2. Viem tuy rang (Pulpitis)\n   - Dau du doi, keo dai\n   - Dau tang ve dem\n\n3. Ap xe rang (Dental Abscess)\n   - Sung ma, dau nhuc lien tuc\n   - Co the sot nhe\n\nKhuyen nghi: Nen kham bac si som de xac dinh chinh xac nguyen nhan.\nHotline: 076.400.9726"
}
```

---

### 2. TRA CUU BANG GIA (Dynamic - Tu Database)

**Vi Du Request**:

```bash
curl -X POST https://pdcms.duckdns.org/api/v1/chatbot/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "cho toi xem bang gia"}'
```

**Response** (lay tu database):

```json
{
  "response": "[BANG GIA DICH VU NHA KHOA]\n\n** Nieng rang **\n- Nieng rang mac cai kim loai: 25.000.000 VND\n- Nieng rang trong suot: 45.000.000 VND\n\n** Tram rang **\n- Tram rang composite: 300.000 VND\n..."
}
```

---

### 3. TIM KIEM DICH VU

**Vi Du Request**:

```bash
curl -X POST https://pdcms.duckdns.org/api/v1/chatbot/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "toi muon lay cao rang"}'
```

---

### 4. FAQ (Chao hoi, Dia chi, Gio lam viec...)

| Knowledge ID    | Keywords                         | Mo Ta               |
| --------------- | -------------------------------- | ------------------- |
| `GREETING`      | "xin chao", "hello", "hi"        | Chao hoi            |
| `ADDRESS`       | "dia chi", "o dau"               | Dia chi phong kham  |
| `WORKING_HOURS` | "gio lam viec", "may gio mo cua" | Thoi gian hoat dong |
| `BOOKING`       | "dat lich", "hen kham"           | Huong dan dat lich  |

---

### 5. TU CHOI CAU HOI NGOAI PHAM VI

Neu nguoi dung hoi cau hoi KHONG lien quan den nha khoa, chatbot se tu choi:

**Vi Du Request**:

```bash
curl -X POST https://pdcms.duckdns.org/api/v1/chatbot/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "thoi tiet hom nay the nao"}'
```

**Response**:

```json
{
  "response": "Xin loi, em la tro ly ao chuyen ve NHA KHOA cua phong kham DenTeeth.\n\nEm co the giup ban:\n- Tra cuu bang gia dich vu\n- Tu van trieu chung rang mieng\n- Thong tin dia chi, gio lam viec\n- Huong dan dat lich kham\n\nAnh/Chi co cau hoi gi ve rang mieng khong a?\nHotline: 076.400.9726"
}
```

---

## LUU DO XU LY CHATBOT

```
Nguoi dung gui tin nhan
         |
         v
+------------------+
| Gemini AI        |
| Phan loai ID     |
+------------------+
         |
         v
+------------------+
| ID = OUT_OF_SCOPE|---> Return: Tu choi ngoai pham vi
+------------------+
         |
         v
+------------------+
| ID = PRICE_LIST  |---> Query Database --> Return: Bang gia
+------------------+
         |
         v
+------------------+
| ID = SERVICE_*   |---> Search Services --> Return: Ket qua tim kiem
+------------------+
         |
         v
+------------------+
| ID = SYMPTOM_*   |---> Return: Phan hoi co dinh (Deterministic)
+------------------+
         |
         v
+------------------+
| ID = FAQ         |---> Query Knowledge Base --> Return: Cau tra loi
+------------------+
```

---

## FRONTEND INTEGRATION

### React/Next.js Example

```javascript
const sendChatMessage = async (message) => {
  try {
    const response = await fetch(
      "https://pdcms.duckdns.org/api/v1/chatbot/chat",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ message }),
      }
    );

    const data = await response.json();
    return data.response;
  } catch (error) {
    console.error("Chatbot error:", error);
    return "Co loi xay ra, vui long thu lai sau.";
  }
};

// Usage
const reply = await sendChatMessage("toi bi dau rang");
console.log(reply);
```

---

## CAU HINH

### Environment Variables

```properties
# application.properties hoac application.yml
chatbot.gemini.api-key=AIzaSy...
chatbot.gemini.model-name=gemini-2.0-flash
```

### Database Tables

1. **chatbot_knowledge** - Luu FAQ responses

   - `knowledge_id`: ID duy nhat (GREETING, ADDRESS, WORKING_HOURS...)
   - `response`: Noi dung tra loi
   - `is_active`: Trang thai active

2. **dental_service** - Luu danh sach dich vu (dung cho PRICE_LIST, SERVICE_INFO)
   - `service_name`: Ten dich vu
   - `price`: Gia dich vu
   - `category_id`: Phan loai

---

## HOTLINE HO TRO

**Phong Kham Nha Khoa DenTeeth**

- Hotline: 076.400.9726
- Dia chi: Lo E2a-7, Duong D1, Khu Cong nghe cao, P. Long Thanh My, TP. Thu Duc, TPHCM
- Gio lam viec: 8:00 - 20:00 (Thu 2 - Thu 7)

---

_Document updated: 2025_
