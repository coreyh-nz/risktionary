# Risktionary User Manual

A guide for researchers and hosts running Risktionary sessions, including studies of AI-generated feedback. The host can use it to explain the game to players.

Screenshots were taken from the current version of the application. Player names are examples, and the host's name and email are shown as "Host Researcher" and "host@example.com".

---

## 1. What Risktionary is

Risktionary is Pictionary for software project risks. In each round one player draws a risk, such as *Burnout*, and everyone else races to guess it in a chat. The group then rates how likely and how severe the risk is, the risk and its description are revealed, and points are awarded.

The game practises three risk management activities:

| Activity | Where it happens in a round |
|---|---|
| **Identify** what could go wrong | Guessing the drawn risk |
| **Assess** how likely and how serious it is | Rating likelihood and severity |
| **Respond**: what would we do about it? | Word review: the risk's description, and class discussion |

While players guess, an AI gives each player personal feedback on their wrong guesses (see [Section 5](#5-ai-feedback)).

### Roles

| Role | Who | Needs an account? |
|---|---|---|
| **Host** | The researcher or teacher running the session. Creates the game, picks the drawer, controls the pace. Does not guess. | Yes: signs in with Google or Microsoft |
| **Drawer** | One player per round, picked by the host from volunteers | No |
| **Guessers** | Every other player | No: joins with the game code and a display name |

---

## 2. Before a session (host)

### 2.1 Sign in

Open the Risktionary website and sign in with **Google** or **Microsoft** at the bottom of the home page. Once you're signed in, the home page shows a **Create Game** link, and the top bar shows **Manage Words** and **Create Game**.

| Not signed in (what players see) | Signed in as host |
|---|---|
| ![Home page, not signed in](images/home-player.png) | ![Home page, signed in](images/home-host.png) |

### 2.2 Prepare the word bank

Each round uses one risk from the word bank. Open **Manage Words** to see the **Word Library**.

![Word library](images/word-bank.png)

- **Add a word:** click **Add Word**, then fill in:
  - **Word**: the risk players must guess (e.g. *Scope Creep*).
  - **Synonyms/Close Matches**: other answers that also count as correct (e.g. *feature creep*, *moving goalposts*). Type one and press **+**. Guesses are matched exactly, ignoring capital letters, so add common variants and spellings.
  - **Description**: shown to everyone after the round, during the word review. Explain what the risk is and how to deal with it. The AI also uses the description when writing feedback.
- **Edit or delete a word:** click **⋯** on its card, then choose **Edit** or **Delete**.

| Word card menu | Editing a word |
|---|---|
| ![Word menu](images/word-menu.png) | ![Edit word](images/word-edit.png) |

> **Tip:** good synonyms matter. In the pilot study, about one in five wrong guesses were typos or close variants (for example "eaerthquake"), and they were marked wrong.

---

## 3. Creating a game (host)

Click **Create Game**. The form has three sections, with a summary card on the right.

![Create game form](images/create-game.png)

1. **Choose your words.** Search for risks and click them to add them. Each word becomes one round. Use the arrows to reorder the rounds, or **Shuffle** them.
2. **Set the pace.** Choose how long each phase lasts:

   | Setting | What it controls | Default |
   |---|---|---|
   | Lobby countdown | Countdown after you press Start Game | 30 s |
   | Drawing | Time to draw and guess | 90 s |
   | Drawing Review | Answer shown with the chat | 10 s |
   | Ranking | Time to rate likelihood and severity | 60 s |
   | Ranking Review | Group results on the risk matrix | 30 s |
   | Word Review | Risk description shown, for discussion | 30 s |
   | Scoring | Round results and feedback | 30 s |

   Choosing **No timer** makes a phase wait until you press **Skip**. The summary card estimates the game's total length.
3. **Player feedback.** Choose **AI Generated** for a feedback study.

![Feedback options](images/create-game-feedback.png)

> **Important: the feedback options currently have no effect.** The backend always uses **AI-generated feedback** with **balanced random allocation**, whatever this section says. Choosing *None*, or *Fixed for all players* with a particular timing and framing, is ignored. Select **AI Generated** and **Balanced random allocation** so the form matches what actually happens. This is a known gap in the code (see the backend `docs/architecture.md`, "Known gaps").

Click **Start game** to open the lobby.

---

## 4. Running a game

### 4.1 Lobby: players join

The host's screen shows a large **game code**. Display it on the projector.

![Empty lobby](images/lobby-empty.png)

Players open the website on their own device, enter the **Game Code** and a **Display Name**, and press **Join Game**. They don't need an account. Each player appears in the lobby as they join.

| Player joining | Host lobby with 5 players |
|---|---|
| ![Join form](images/join.png) | ![Lobby with players](images/lobby-host.png) |

When everyone has joined, the host presses **Start Game**. A countdown runs (the host can **Skip** it), during which late players can still join.

![Starting countdown](images/starting-countdown.png)

> **Study note:** each player's feedback condition is assigned **when they join** (see [Section 5.1](#51-how-feedback-conditions-are-assigned)).

### 4.2 Choosing the drawer

At the start of each round, players who want to draw press **Raise Hand**. The host sees the volunteers, selects one, and presses **Pick _name_**.

| Player: Raise Hand | Player: volunteered | Host: picking the drawer |
|---|---|---|
| ![Volunteer](images/volunteer.png) | ![Volunteered](images/volunteered.png) | ![Pick drawer](images/pick-drawer.png) |

### 4.3 Drawing and guessing

**Drawer.** Only the drawer sees the word (top centre). They draw on the canvas with the pen, eraser and colour palette below it. They can't type guesses.

| Drawer before drawing | Drawer while drawing |
|---|---|
| ![Drawer view](images/drawer-view.png) | ![Drawing](images/drawer-drawing.png) |

**Guessers** see the drawing live. The word is shown as blank boxes, one per letter. They type guesses in **Type a guess...** and press Enter. Every guess appears in the **Chat & Guesses** panel for everyone.

![Guesser view](images/guesser-view.png)

A correct guess shows a **Correct!** pop-up with the points earned. The word itself is not shown to other players. Faster correct guesses earn more points, and the drawer earns the average of the correct guessers' points.

![Correct guess](images/correct-guess.png)

**Host.** The host sees the drawing, the chat, the timer (top left) and **Skip** (top right). The drawing phase ends when the timer runs out, when every guesser has guessed correctly, or when the host presses **Skip**.

![Host during drawing](images/drawing-host.png)

> **Keep the host screen open throughout the drawing phase.** The host's browser sends snapshots of the drawing for the AI to interpret. If the host tab is closed or minimised for a long time, feedback can't refer to the drawing.

### 4.4 Drawing review

Players who didn't guess the word see a **Time's up!** pop-up. The chat then reveals the answer ("Time's up! The word was ...").

| Time's up pop-up | Drawing review (host) |
|---|---|
| ![Time's up](images/times-up.png) | ![Drawing review](images/drawing-review.png) |

### 4.5 Rating the risk

Each player rates **how likely** the risk is to occur (Rare to Almost Certain) and **how severe** its impact would be (Insignificant to Catastrophic), then presses **Lock in vote**. When everyone has voted, the game moves on.

![Rating the risk](images/rating-vote.png)

**Ranking review** shows how the room voted on a likelihood × severity risk matrix. The number in each cell is how many players chose it. This is a good moment for discussion: why did people disagree?

![How the room voted](images/ranking-review.png)

### 4.6 Word review

Everyone sees the risk, its synonyms and its description. Use it to discuss how a team would **respond** to the risk.

![Word review](images/word-review.png)

### 4.7 Scoring

The round results show the podium and everyone's points. Players with AI feedback also see an **AI feedback** card below the results (see [Section 5](#5-ai-feedback)). The next round then starts with drawer selection.

![Round results (host)](images/scoring-host.png)

### 4.8 End of the game

After the last round, everyone sees **Final standings**. Players also see a **"You're part of a study"** card with a **Take the survey** button, if a survey link has been configured (see [Section 6.3](#63-survey-link)).

| Host | Player |
|---|---|
| ![Game over (host)](images/game-over-host.png) | ![Game over (player)](images/game-over-player.png) |

---

## 5. AI feedback

When a player guesses wrong, the game uses an AI model to write feedback that helps them understand the risk without giving the answer away. Feedback is private: each player sees only their own. Every message is labelled **AI feedback**.

**The AI never decides whether a guess is right.** That is a plain text match against the word and its synonyms (ignoring capital letters). The AI only writes the explanation.

### 5.1 How feedback conditions are assigned

Each player is given one of **six combinations** of timing and framing when they join a game. The game picks at random among the combinations with the fewest players so far, so the groups stay balanced. A player keeps their combination for the whole game. In a new game they may get a different one.

| | **Positive** | **Corrective** | **Neutral** |
|---|---|---|---|
| **Instant**: a reply under each wrong guess | 1 | 2 | 3 |
| **Delayed**: one summary at the end of the round | 4 | 5 | 6 |

- **Positive** acknowledges the guess as a reasonable attempt before pointing in the right direction.
- **Corrective** states plainly how the guess differs from the answer.
- **Neutral** states the same facts without reacting to the guess.

Players can't see their own combination. Researchers can find it in the database (see [Section 6.4](#64-getting-the-data-out)).

### 5.2 Instant feedback

A few seconds after a wrong guess, a reply appears **directly under the player's own guess** in the chat. It never names the answer, and it gets more specific as time runs out.

| Instant feedback in the chat | Close-up |
|---|---|
| ![Instant feedback](images/instant-feedback.png) | ![Close-up](images/instant-feedback-closeup.png) |

### 5.3 Delayed feedback

Players with delayed feedback get nothing during drawing. At **Scoring**, their **AI feedback** card shows a **Round summary** that names the answer and explains how their guesses related to it.

| Delayed: round summary | Instant: the round's messages again |
|---|---|
| ![Delayed feedback](images/scoring-delayed.png) | ![Instant feedback at scoring](images/scoring-instant.png) |

### 5.4 Feedback history

The **Feedback** button (bottom right, with a count of unread messages) opens the **Feedback history**: every message the player has received, grouped by round. Instant messages scroll out of view in a busy chat, so remind players it is there.

![Feedback history](images/feedback-history.png)

> Players who guess correctly straight away receive no feedback, because feedback is only written for wrong guesses.

---

## 6. Running a study session

### 6.1 Checklist

**Before the session**
- [ ] Ethics approval covers the session, and participant information and consent are ready.
- [ ] The site is up, and you can sign in as host.
- [ ] The AI provider key has credit. One game of about 8 rounds used roughly 200k tokens in the pilot.
- [ ] The word bank has the risks you want, with synonyms and descriptions.
- [ ] The survey link is configured (Section 6.3).
- [ ] The **backend was restarted, or no other game has been hosted from your account in the last 5 minutes** (see Section 7).
- [ ] Create the game: AI Generated, Balanced random allocation, and suitable timers.

**During the session**
- [ ] Project the host screen and keep it open in the foreground.
- [ ] Explain the game using Sections 1 and 4, and point out the **Feedback** button.
- [ ] Wait until everyone has joined before pressing Start Game. Players who join late still get a condition, but miss rounds.
- [ ] Encourage players to keep guessing. Feedback only comes from wrong guesses.

**After the session**
- [ ] Give players time for the survey on the final screen.
- [ ] Export the data (Section 6.4).

### 6.2 Several games in one session

Each game assigns conditions afresh, so a returning player may get a different combination in the second game. If your study needs each player to keep the same condition, run **one game** with more rounds.

### 6.3 Survey link

The **Take the survey** button is set when the frontend is built and deployed. It isn't set in the app:
- `NEXT_PUBLIC_SURVEY_URL`: build-time setting that shows the survey prompt.
- `SURVEY_REDIRECT_URL`: run-time setting giving where `/survey` redirects.

See the backend `docs/deployment.md`. To link survey answers to conditions, the survey needs a way to identify the player, which the current version doesn't provide. Plan this before the study (for example, ask players to enter their display name and game code in the survey).

### 6.4 Getting the data out

Everything is saved to the database during play: every guess and its time, chat, ratings, drawing snapshots with the AI's interpretation, each feedback message (the neutral fact note and the final text) with the player's condition, and AI token use. Players are stored only as a random ID per game. Display names are **not** saved.

- **Through the website (researcher role):** `GET /api/v1/game` lists games, and `GET /api/v1/game/{gameId}/details` returns everything for one game as JSON.
- **From PostgreSQL directly**, which is best for analysis. Conditions are in `risktionary_game_player` (`feedback_timing_condition`, `feedback_framing_condition`), and the feedback text is in `risktionary_game_round_feedback`.

Full details, including how to grant the researcher role, are in the backend `docs/deployment.md`, section 9.

---

## 7. Troubleshooting and known issues

| Problem | Cause and fix |
|---|---|
| After creating a new game, the host's screen shows the **previous game** (e.g. its final standings) | A known bug: the backend finds the host's game by account, and finished games are only removed 5 minutes after the host leaves. **Fix:** close the old game's tab and wait 5 minutes before creating a new game, or restart the backend. |
| No feedback appears | Feedback is only written for **wrong** guesses, and only instant players see it during drawing. Check the AI key has credit. Restarting the backend ends all running games. |
| A player can't join ("display name in use") | Two players in a game can't have the same display name. Ask them to add an initial. |
| A sensible guess was marked wrong | Matching is exact (ignoring capital letters). Add the variant as a synonym for next time. |
| A player's screen is stuck or disconnected | Refreshing the page reconnects them to the same game in the same browser tab. |
| Feedback refers to the drawing oddly or not at all | Drawing snapshots come from the **host's** browser. Keep the host tab open and visible. |
| The create-game feedback settings didn't apply | Known gap: the backend always uses AI feedback with balanced random allocation (Section 3). |
| Restarting the backend during a session | All games in progress end. Data from completed rounds has already been saved. |

---

## 8. Quick reference for explaining the game to players

1. Go to the website, and enter the **game code** on the screen and a **name**. No account is needed.
2. Each round: **Raise Hand** if you want to draw. The host picks one drawer.
3. **Drawer:** draw the risk shown at the top. No letters or words.
4. **Everyone else:** type guesses in the chat. Faster correct guesses score more.
5. Wrong guesses may get **AI feedback**, either straight away under your guess or as a summary after the round. Open **Feedback** (bottom right) to see it all again.
6. **Rate** how likely and how severe the risk is, see how the room voted, and read the risk's description.
7. At the end, please **take the survey**.
