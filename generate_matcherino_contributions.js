// Copy and paste this script in your browser's console while on https://matcherino.com/t/mota/contributions
// to generate the Matcherino contributions list.

const donations = document.getElementsByClassName('list-donation-item');
let output = '';

for (let i = 0; i < donations.length; i++) {
    const name = donations[i].getElementsByClassName('user-link-name')[0].innerText;

    const amounts = donations[i].getElementsByClassName('amount');
    const contribution = parseFloat(amounts[0].innerText.substring(1));
    const type = amounts[1].innerText;

    const comment = donations[i].getElementsByClassName('flex-3')[0].innerText;

    if (type === "Direct Contribution" && contribution >= 1.0 && name !== "MightyTeapot") {
        output += name + "\t" + contribution + "\t" + type + "\t" + comment + "\n";
    }
}

console.log(output)